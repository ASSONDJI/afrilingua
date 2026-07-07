class AnswerResult {
  final String questionId;
  final bool correct;
  final String correctAnswer;

  AnswerResult({
    required this.questionId,
    required this.correct,
    required this.correctAnswer,
  });

  factory AnswerResult.fromJson(Map<String, dynamic> json) {
    return AnswerResult(
      questionId: json['questionId'] as String,
      correct: json['correct'] as bool,
      correctAnswer: json['correctAnswer'] as String,
    );
  }
}