import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/lesson_model.dart';

class LessonRepository {
  final Dio _dio;

  LessonRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<List<Lesson>> listByLanguage(String languageId) async {
    final response = await _dio.get(
      '/api/lessons',
      queryParameters: {'languageId': languageId},
    );
    return (response.data as List)
        .map((json) => Lesson.fromJson(json as Map<String, dynamic>))
        .toList();
  }
}
