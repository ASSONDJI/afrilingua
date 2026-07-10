import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/badge_catalog_model.dart';
import '../domain/progression_model.dart';

class GamificationRepository {
  final Dio _dio;

  GamificationRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<Progression> loadProgression(String userId) async {
    final response = await _dio.get('/api/recommendations/progressions/$userId');
    return Progression.fromJson(response.data as Map<String, dynamic>);
  }

  Future<List<BadgeCatalogEntry>> loadBadgeCatalog() async {
    final response = await _dio.get('/api/recommendations/badges');
    return (response.data as List)
        .cast<Map<String, dynamic>>()
        .map(BadgeCatalogEntry.fromJson)
        .toList();
  }
}
