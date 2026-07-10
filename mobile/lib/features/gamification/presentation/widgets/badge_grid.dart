import 'package:flutter/material.dart';
import '../../../../core/theme/app_theme.dart';
import '../../domain/badge_catalog_model.dart';
import '../../domain/progression_model.dart' as domain;

IconData _iconForCriteriaType(String criteriaType) {
  switch (criteriaType) {
    case 'streak':
      return Icons.local_fire_department_rounded;
    case 'lessons_completed':
      return Icons.flag_rounded;
    case 'words_learned':
      return Icons.auto_stories_rounded;
    case 'quiz_perfect':
      return Icons.emoji_events_rounded;
    default:
      return Icons.star_rounded;
  }
}

class BadgeGrid extends StatelessWidget {
  final List<BadgeCatalogEntry> catalog;
  final List<domain.Badge> earned;

  const BadgeGrid({super.key, required this.catalog, required this.earned});

  @override
  Widget build(BuildContext context) {
    final earnedByCode = {for (final b in earned) b.code: b};

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 0.95,
      ),
      itemCount: catalog.length,
      itemBuilder: (context, index) {
        final entry = catalog[index];
        final earnedBadge = earnedByCode[entry.code];
        return _BadgeCard(entry: entry, earnedBadge: earnedBadge);
      },
    );
  }
}

class _BadgeCard extends StatelessWidget {
  final BadgeCatalogEntry entry;
  final domain.Badge? earnedBadge;

  const _BadgeCard({required this.entry, this.earnedBadge});

  @override
  Widget build(BuildContext context) {
    final isUnlocked = earnedBadge != null;

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: isUnlocked
              ? AppTheme.accentAmber.withValues(alpha: 0.4)
              : Colors.black12,
        ),
        boxShadow: isUnlocked
            ? [
                BoxShadow(
                  color: AppTheme.accentAmber.withValues(alpha: 0.15),
                  blurRadius: 10,
                  offset: const Offset(0, 4),
                ),
              ]
            : null,
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 52,
            height: 52,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: isUnlocked
                  ? const LinearGradient(
                      colors: [AppTheme.accentAmber, Color(0xFFFFC94D)],
                    )
                  : null,
              color: isUnlocked ? null : Colors.grey.shade200,
            ),
            child: Icon(
              isUnlocked
                  ? _iconForCriteriaType(entry.criteriaType)
                  : Icons.lock_rounded,
              color: isUnlocked ? Colors.white : Colors.grey.shade400,
              size: 26,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            entry.name,
            textAlign: TextAlign.center,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              fontWeight: FontWeight.w700,
              fontSize: 13,
              color: isUnlocked ? Colors.black87 : Colors.grey.shade500,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            isUnlocked ? 'Débloqué' : entry.description,
            textAlign: TextAlign.center,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              fontSize: 11,
              color: isUnlocked
                  ? AppTheme.primaryGreen
                  : Colors.grey.shade400,
              fontWeight: isUnlocked ? FontWeight.w600 : FontWeight.normal,
            ),
          ),
        ],
      ),
    );
  }
}
