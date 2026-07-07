import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../domain/answer_result_model.dart';
import '../domain/question_model.dart';
import 'quiz_providers.dart';

class QuizScreen extends ConsumerStatefulWidget {
  final String lessonId;
  final String? lessonTitle;

  const QuizScreen({super.key, required this.lessonId, this.lessonTitle});

  @override
  ConsumerState<QuizScreen> createState() => _QuizScreenState();
}

class _QuizScreenState extends ConsumerState<QuizScreen> {
  int _currentIndex = 0;
  String? _selectedOption;
  AnswerResult? _result;
  bool _submitting = false;

  Future<void> _submit(Question question) async {
    if (_selectedOption == null || _submitting) return;

    setState(() => _submitting = true);
    try {
      final result = await ref
          .read(quizRepositoryProvider)
          .submitAnswer(question.id, _selectedOption!);
      setState(() => _result = result);
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

  void _next(int totalQuestions) {
    if (_currentIndex < totalQuestions - 1) {
      setState(() {
        _currentIndex++;
        _selectedOption = null;
        _result = null;
      });
    } else {
      Navigator.of(context).pop();
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

          final question = questions[_currentIndex];

          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Question ${_currentIndex + 1}/${questions.length}',
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
                      _result!.correct ? 'Bonne réponse !' : 'Réponse correcte : ${_result!.correctAnswer}',
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
                        : () => _next(questions.length),
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