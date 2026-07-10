import 'package:flutter/material.dart';
import '../../../../core/theme/app_theme.dart';
import '../../domain/progression_model.dart';

class ProgressionHeader extends StatelessWidget {
  final Progression progression;

  const ProgressionHeader({super.key, required this.progression});

  @override
  Widget build(BuildContext context) {
    final progressRatio =
        progression.xpIntoCurrentLevel / progression.xpNeededForNextLevel;

    return Container(
      margin: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [AppTheme.primaryGreen, Color(0xFF14512F)],
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: AppTheme.primaryGreen.withValues(alpha: 0.35),
            blurRadius: 16,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _LevelBadge(level: progression.level),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Niveau ${progression.level}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${progression.xp} XP au total',
                      style: TextStyle(
                        color: Colors.white.withValues(alpha: 0.85),
                        fontSize: 13,
                      ),
                    ),
                  ],
                ),
              ),
              _StreakCounter(streak: progression.currentStreak),
            ],
          ),
          const SizedBox(height: 20),
          _XpProgressBar(ratio: progressRatio.clamp(0.0, 1.0)),
          const SizedBox(height: 6),
          Text(
            '${progression.xpIntoCurrentLevel} / ${progression.xpNeededForNextLevel} XP avant le niveau ${progression.level + 1}',
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.75),
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}

class _LevelBadge extends StatelessWidget {
  final int level;
  const _LevelBadge({required this.level});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 56,
      height: 56,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: Colors.white.withValues(alpha: 0.15),
        border: Border.all(color: AppTheme.accentAmber, width: 2.5),
      ),
      child: Center(
        child: Text(
          '$level',
          style: const TextStyle(
            color: Colors.white,
            fontSize: 24,
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
    );
  }
}

class _StreakCounter extends StatelessWidget {
  final int streak;
  const _StreakCounter({required this.streak});

  @override
  Widget build(BuildContext context) {
    final isActive = streak > 0;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: isActive
            ? AppTheme.accentAmber.withValues(alpha: 0.2)
            : Colors.white.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        children: [
          Icon(
            Icons.local_fire_department_rounded,
            color: isActive ? AppTheme.accentAmber : Colors.white54,
            size: 24,
          ),
          const SizedBox(height: 2),
          Text(
            '$streak',
            style: TextStyle(
              color: isActive ? Colors.white : Colors.white54,
              fontWeight: FontWeight.w800,
              fontSize: 15,
            ),
          ),
        ],
      ),
    );
  }
}

class _XpProgressBar extends StatelessWidget {
  final double ratio;
  const _XpProgressBar({required this.ratio});

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(8),
      child: Container(
        height: 12,
        color: Colors.white.withValues(alpha: 0.15),
        child: TweenAnimationBuilder<double>(
          tween: Tween(begin: 0, end: ratio),
          duration: const Duration(milliseconds: 700),
          curve: Curves.easeOutCubic,
          builder: (context, value, _) {
            return FractionallySizedBox(
              alignment: Alignment.centerLeft,
              widthFactor: value,
              child: Container(
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppTheme.accentAmber, Color(0xFFFFC94D)],
                  ),
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
