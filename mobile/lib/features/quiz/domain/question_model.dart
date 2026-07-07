class Question {
  final String id;
  final String lessonId;
  final String type;
  final String questionText;
  final List<String> options;

  Question({
    required this.id,
    required this.lessonId,
    required this.type,
    required this.questionText,
    required this.options,
  });

  factory Question.fromJson(Map<String, dynamic> json) {
    return Question(
      id: json['id'] as String,
      lessonId: json['lessonId'] as String,
      type: json['type'] as String,
      questionText: json['questionText'] as String,
      options: (json['options'] as List<dynamic>? ?? []).cast<String>(),
    );
  }
}