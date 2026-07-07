class Lesson {
  final String id;
  final String languageId;
  final String title;
  final int order;
  final int level;
  final List<String> wordIds;

  Lesson({
    required this.id,
    required this.languageId,
    required this.title,
    required this.order,
    required this.level,
    required this.wordIds,
  });

  factory Lesson.fromJson(Map<String, dynamic> json) {
    return Lesson(
      id: json['id'] as String,
      languageId: json['languageId'] as String,
      title: json['title'] as String,
      order: json['order'] as int,
      level: json['level'] as int,
      wordIds: (json['wordIds'] as List<dynamic>? ?? []).cast<String>(),
    );
  }
}
