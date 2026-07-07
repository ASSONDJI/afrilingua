import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/languages/presentation/languages_screen.dart';
import '../../features/lessons/presentation/lessons_screen.dart';
import '../../features/quiz/presentation/quiz_screen.dart';

final routerProvider = Provider((ref) {
  return GoRouter(
    initialLocation: '/login',
    routes: [
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
      GoRoute(path: '/register', builder: (context, state) => const RegisterScreen()),
      GoRoute(path: '/lessons', builder: (context, state) => const LanguagesScreen()),
      GoRoute(
        path: '/quiz/:lessonId',
        builder: (context, state) {
          final lessonId = state.pathParameters['lessonId']!;
          final lessonTitle = state.extra as String?;
          return QuizScreen(lessonId: lessonId, lessonTitle: lessonTitle);
        },
      ),
      GoRoute(
        path: '/lessons/:languageId',
        builder: (context, state) {
          final languageId = state.pathParameters['languageId']!;
          final languageName = state.extra as String?;
          return LessonsScreen(languageId: languageId, languageName: languageName);
        },
      ),
    ],
  );
});