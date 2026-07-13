import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/languages/presentation/languages_screen.dart';
import '../../features/lessons/presentation/lessons_screen.dart';
import '../../features/quiz/presentation/quiz_screen.dart';
import '../../features/user/presentation/profile_screen.dart';
import '../../features/gamification/presentation/gamification_screen.dart';
import '../../features/statistics/presentation/statistics_screen.dart';

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
          final extra = state.extra as Map<String, dynamic>?;
          return QuizScreen(
            lessonId: lessonId,
            lessonTitle: extra?['title'] as String?,
            languageId: extra?['languageId'] as String?,
          );
        },
      ),
      GoRoute(
        path: '/statistics/:languageId',
        builder: (context, state) {
          final languageId = state.pathParameters['languageId']!;
          final languageName = state.extra as String?;
          return StatisticsScreen(languageId: languageId, languageName: languageName);
        },
      ),
      GoRoute(path: '/profile', builder: (context, state) => const ProfileScreen()),
      GoRoute(path: '/progression', builder: (context, state) => const GamificationScreen()),
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