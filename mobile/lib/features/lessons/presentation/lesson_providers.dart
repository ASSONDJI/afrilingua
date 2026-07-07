import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/lesson_repository.dart';
import '../domain/lesson_model.dart';

final lessonRepositoryProvider = Provider((ref) {
  return LessonRepository(ref.watch(apiClientProvider));
});

final lessonsByLanguageProvider =
    FutureProvider.family<List<Lesson>, String>((ref, languageId) async {
  return ref.watch(lessonRepositoryProvider).listByLanguage(languageId);
});

