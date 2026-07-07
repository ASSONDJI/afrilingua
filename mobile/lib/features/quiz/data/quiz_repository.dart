import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/answer_result_model.dart';
import '../domain/question_model.dart';

class QuizRepository {
  final Dio _dio;

  QuizRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<List<Question>> listByLesson(String lessonId) async {
    final response = await _dio.get(
      '/api/quizzes',
      queryParameters: {'lessonId': lessonId},
    );
    return (response.data as List)
        .map((json) => Question.fromJson(json as Map<String, dynamic>))
        .toList();
  }

  Future<AnswerResult> submitAnswer(String questionId, String submittedAnswer) async {
    final response = await _dio.post(
      '/api/quizzes/$questionId/submit',
      data: {'submittedAnswer': submittedAnswer},
    );
    return AnswerResult.fromJson(response.data as Map<String, dynamic>);
  }
}