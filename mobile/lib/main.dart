import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';
import 'features/auth/presentation/auth_providers.dart';

Future<void> main() async {
  // Restaure la session AVANT de construire l'arbre de widgets, pour que
  // go_router (via MaterialApp.router) evalue l'URL initiale du navigateur
  // avec un accountIdProvider deja peuple. Ca evite a la fois le flash
  // "login -> ecran demande" et le bug de parsing d'URL introduit par une
  // precedente tentative avec un MaterialApp non-router imbrique.
  final container = ProviderContainer();
  await container.read(authControllerProvider.notifier).restoreSession();

  runApp(
    UncontrolledProviderScope(
      container: container,
      child: const AfriLinguaApp(),
    ),
  );
}

class AfriLinguaApp extends ConsumerWidget {
  const AfriLinguaApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    return MaterialApp.router(
      title: 'AfriLingua',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      routerConfig: router,
    );
  }
}
