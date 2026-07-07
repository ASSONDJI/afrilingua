import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/language_model.dart';

class LanguageRepository {
  final Dio _dio;

  LanguageRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<List<Language>> listAll() async {
    final response = await _dio.get('/api/content/languages');
    return (response.data as List)
        .map((json) => Language.fromJson(json as Map<String, dynamic>))
        .toList();
  }
}
