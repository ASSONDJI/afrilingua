import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_theme.dart';
import '../../gamification/domain/progression_model.dart';
import '../../gamification/presentation/gamification_providers.dart';
import 'package:go_router/go_router.dart';

class LessonCompleteScreen extends ConsumerStatefulWidget {
  final String? lessonTitle;
  final String languageId;

  const LessonCompleteScreen({super.key, this.lessonTitle, required this.languageId});

  @override
  ConsumerState<LessonCompleteScreen> createState() => _LessonCompleteScreenState();
}

class _LessonCompleteScreenState extends ConsumerState<LessonCompleteScreen> {
  Progression? _before;
  Progression? _after;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _resolveXpGain();
  }

  /// The XP-crediting event travels through RabbitMQ asynchronously, so it
  /// may not have been processed yet by the time this screen opens right
  /// after the last correct answer. Rather than guessing a number, this
  /// polls the real progression a few times with short delays until it
  /// changes (or gives up and shows the latest value anyway).
  Future<void> _resolveXpGain() async {
    final repo = ref.read(gamificationRepositoryProvider);
    final userId = ref.read(accountIdProviderForGamification);
    if (userId == null) {
      setState(() => _loading = false);
      return;
    }

    final before = ref.read(lastKnownProgressionSnapshotProvider);
    Progression? latest = before;

    for (var attempt = 0; attempt < 6; attempt++) {
      await Future.delayed(const Duration(milliseconds: 500));
      try {
        final fetched = await repo.loadProgression(userId);
        latest = fetched;
        if (before == null || fetched.xp != before.xp) break;
      } catch (_) {
        break;
      }
    }

    if (mounted) {
      setState(() {
        _before = before;
        _after = latest;
        _loading = false;
      });
      ref.read(lastKnownProgressionSnapshotProvider.notifier).state = latest;
    }
  }

  @override
  Widget build(BuildContext context) {
    final xpGained = (_after != null && _before != null) ? _after!.xp - _before!.xp : null;

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                width: 96,
                height: 96,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    colors: [AppTheme.accentAmber, Color(0xFFFFC94D)],
                  ),
                ),
                child: const Icon(Icons.emoji_events_rounded, color: Colors.white, size: 52),
              ),
              const SizedBox(height: 24),
              Text(
                'Leçon terminée !',
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w800,
                  color: AppTheme.primaryGreen,
                ),
              ),
              if (widget.lessonTitle != null) ...[
                const SizedBox(height: 6),
                Text(
                  widget.lessonTitle!,
                  style: const TextStyle(fontSize: 15, color: Colors.black54),
                  textAlign: TextAlign.center,
                ),
              ],
              const SizedBox(height: 32),
              if (_loading)
                const CircularProgressIndicator()
              else
                _XpSummaryCard(xpGained: xpGained, after: _after),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => context.go('/lessons/${widget.languageId}'),
                  child: const Text('Continuer'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _XpSummaryCard extends StatelessWidget {
  final int? xpGained;
  final Progression? after;

  const _XpSummaryCard({required this.xpGained, required this.after});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppTheme.primaryGreen.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          Text(
            xpGained != null ? '+$xpGained XP' : 'XP en cours de calcul…',
            style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w900, color: AppTheme.primaryGreen),
          ),
          if (after != null) ...[
            const SizedBox(height: 8),
            Text('Niveau ${after!.level} · Série ${after!.currentStreak} jour(s)'),
          ],
        ],
      ),
    );
  }
}
