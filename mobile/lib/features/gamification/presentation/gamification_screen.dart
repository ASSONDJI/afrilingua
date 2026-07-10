import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'gamification_providers.dart';
import 'widgets/badge_grid.dart';
import 'widgets/progression_header.dart';

class GamificationScreen extends ConsumerWidget {
  const GamificationScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dataAsync = ref.watch(gamificationDataProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Ma progression')),
      body: dataAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              'Impossible de charger la progression.\n\n$error',
              textAlign: TextAlign.center,
            ),
          ),
        ),
        data: (data) {
          final (progression, catalog) = data;
          return RefreshIndicator(
            onRefresh: () async => ref.invalidate(gamificationDataProvider),
            child: ListView(
              children: [
                ProgressionHeader(progression: progression),
                const Padding(
                  padding: EdgeInsets.fromLTRB(16, 12, 16, 8),
                  child: Text(
                    'Badges',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
                  ),
                ),
                BadgeGrid(catalog: catalog, earned: progression.badges),
                const SizedBox(height: 24),
              ],
            ),
          );
        },
      ),
    );
  }
}
