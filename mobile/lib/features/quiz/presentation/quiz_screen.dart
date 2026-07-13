import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/answer_result_model.dart';
import '../domain/question_model.dart';
import 'quiz_providers.dart';
import 'lesson_complete_screen.dart';
import '../../auth/presentation/auth_providers.dart';
import '../../user/presentation/user_providers.dart';

class QuizScreen extends ConsumerStatefulWidget {
  final String lessonId;
  final String? lessonTitle;
  final String? languageId;

  const QuizScreen({super.key, required this.lessonId, this.lessonTitle, this.languageId});

  @override
  ConsumerState<QuizScreen> createState() => _QuizScreenState();
}

class _QuizScreenState extends ConsumerState<QuizScreen> {
  /// Queue-based flow (Duolingo-style): a wrong answer re-queues the same
  /// question at the END of the queue instead of advancing, so the learner
  /// keeps seeing it (mixed with the others) until answered correctly.
  /// This matches the backend's completion rule: a lesson only completes
  /// once every question has at least one correct attempt.
  final Queue<Question> _queue = Queue<Question>();
  int _totalQuestions = 0;
  int _questionsRemaining = 0;
  bool _initialized = false;

  String? _selectedOption;
  AnswerResult? _result;
  bool _submitting = false;

  void _initQueue(List<Question> questions) {
    if (_initialized) return;
    _queue.addAll(questions);
    _totalQuestions = questions.length;
    _questionsRemaining = questions.length;
    _initialized = true;
  }

  Future<void> _submit(Question question) async {
    if (_selectedOption == null || _submitting) return;

    setState(() => _submitting = true);
    try {
      final result = await ref
          .read(quizRepositoryProvider)
          .submitAnswer(question.id, _selectedOption!);
      setState(() => _result = result);

      final accountId = ref.read(accountIdProvider);
      if (accountId != null) {
        // Best-effort: activity logging must never block the quiz flow.
        ref.read(userRepositoryProvider).logActivity(
              accountId: accountId,
              activityType: 'QUIZ_ATTEMPTED',
              metadata: 'questionId=${question.id};correct=${result.correct}',
            ).catchError((_) {});
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur lors de la soumission : $error')),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  void _next() {
    final wasCorrect = _result?.correct ?? false;
    final finishedQuestion = _queue.removeFirst();

    if (wasCorrect) {
      _questionsRemaining--;
    } else {
      // Re-queue at the end: the learner will see it again, mixed with
      // the remaining questions, until they get it right.
      _queue.addLast(finishedQuestion);
    }

    setState(() {
      _selectedOption = null;
      _result = null;
    });

    if (_queue.isEmpty) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (_) => LessonCompleteScreen(
            lessonTitle: widget.lessonTitle,
            languageId: widget.languageId ?? '',
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final questionsAsync = ref.watch(questionsByLessonProvider(widget.lessonId));

    return Scaffold(
      appBar: AppBar(title: Text(widget.lessonTitle ?? 'Quiz')),
      body: questionsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text('Impossible de charger le quiz.\n\n$error', textAlign: TextAlign.center),
          ),
        ),
        data: (questions) {
          if (questions.isEmpty) {
            return const Center(child: Text('Aucune question disponible pour cette leçon.'));
          }

          _initQueue(questions);

          if (_queue.isEmpty) {
            // Guards the brief frame between the last correct answer and
            // the navigation to LessonCompleteScreen taking effect.
            return const Center(child: CircularProgressIndicator());
          }

          final question = _queue.first;
          final progressDone = _totalQuestions - _questionsRemaining;

          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: LinearProgressIndicator(
                    value: _totalQuestions == 0 ? 0 : progressDone / _totalQuestions,
                    minHeight: 8,
                    backgroundColor: Theme.of(context).colorScheme.primary.withValues(alpha: 0.15),
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  '$progressDone/$_totalQuestions',
                  style: TextStyle(color: Theme.of(context).colorScheme.primary, fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 12),
                Text(
                  question.questionText,
                  style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 24),
                ...question.options.map((option) {
                  final isSelected = _selectedOption == option;
                  Color? tileColor;
                  if (_result != null) {
                    if (option == _result!.correctAnswer) {
                      tileColor = Colors.green.shade100;
                    } else if (isSelected) {
                      tileColor = Colors.red.shade100;
                    }
                  } else if (isSelected) {
                    tileColor = Theme.of(context).colorScheme.primary.withValues(alpha: 0.1);
                  }

                  return Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Card(
                      color: tileColor,
                      elevation: 0,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                      child: ListTile(
                        contentPadding: const EdgeInsets.all(16),
                        title: Text(option),
                        onTap: _result == null
                            ? () => setState(() => _selectedOption = option)
                            : null,
                      ),
                    ),
                  );
                }),
                const Spacer(),
                if (_result != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      _result!.correct
                          ? 'Bonne réponse !'
                          : 'Pas tout à fait : ${_result!.correctAnswer}. On la reverra plus tard.',
                      style: TextStyle(
                        color: _result!.correct ? Colors.green.shade800 : Colors.red.shade800,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _result == null
                        ? (_selectedOption == null ? null : () => _submit(question))
                        : _next,
                    child: _submitting
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : Text(_result == null ? 'Valider' : 'Continuer'),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
