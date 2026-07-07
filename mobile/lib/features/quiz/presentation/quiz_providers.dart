import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/quiz_repository.dart';
import '../domain/question_model.dart';

final quizRepositoryProvider = Provider((ref) {
  return QuizRepository(ref.watch(apiClientProvider));
});

final questionsByLessonProvider =
    FutureProvider.family<List<Question>, String>((ref, lessonId) async {
  return ref.watch(quizRepositoryProvider).listByLesson(lessonId);
});