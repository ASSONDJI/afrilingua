class Badge {
  final String code;
  final String name;
  final DateTime earnedAt;

  Badge({required this.code, required this.name, required this.earnedAt});

  factory Badge.fromJson(Map<String, dynamic> json) {
    return Badge(
      code: json['code'] as String,
      name: json['name'] as String,
      earnedAt: DateTime.parse(json['earned_at'] as String),
    );
  }
}

class Progression {
  final int xp;
  final int level;
  final int currentStreak;
  final int longestStreak;
  final int lessonsCompleted;
  final bool hasPerfectQuiz;
  final List<Badge> badges;

  Progression({
    required this.xp,
    required this.level,
    required this.currentStreak,
    required this.longestStreak,
    required this.lessonsCompleted,
    required this.hasPerfectQuiz,
    required this.badges,
  });

  factory Progression.fromJson(Map<String, dynamic> json) {
    return Progression(
      xp: json['xp'] as int,
      level: json['level'] as int,
      currentStreak: json['current_streak'] as int,
      longestStreak: json['longest_streak'] as int,
      lessonsCompleted: json['lessons_completed'] as int,
      hasPerfectQuiz: json['has_perfect_quiz'] as bool,
      badges: (json['badges'] as List)
          .cast<Map<String, dynamic>>()
          .map(Badge.fromJson)
          .toList(),
    );
  }

  /// XP restant avant le niveau suivant, pour une barre de progression.
  /// Chaque niveau necessite 100 XP (voir compute_level cote backend :
  /// floor(xp/100)+1), donc le seuil du niveau courant est (level-1)*100.
  int get xpIntoCurrentLevel => xp - (level - 1) * 100;
  int get xpNeededForNextLevel => 100;
}
