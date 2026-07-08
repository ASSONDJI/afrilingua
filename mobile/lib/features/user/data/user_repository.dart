import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/activity_model.dart';
import '../domain/user_preferences_model.dart';
import '../domain/user_profile_model.dart';

class UserRepository {
  final Dio _dio;

  UserRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<UserProfile> createProfile({
    required String accountId,
    required String displayName,
  }) async {
    final response = await _dio.post('/api/users', data: {
      'accountId': accountId,
      'displayName': displayName,
    });
    return UserProfile.fromJson(response.data as Map<String, dynamic>);
  }

  Future<UserProfile> getProfile(String accountId) async {
    final response = await _dio.get('/api/users/$accountId');
    return UserProfile.fromJson(response.data as Map<String, dynamic>);
  }

  Future<UserPreferences> getPreferences(String accountId) async {
    final response = await _dio.get('/api/users/$accountId/preferences');
    return UserPreferences.fromJson(response.data as Map<String, dynamic>);
  }

  Future<void> logActivity({
    required String accountId,
    required String activityType,
    String? metadata,
  }) async {
    await _dio.post('/api/users/$accountId/activities', data: {
      'activityType': activityType,
      'metadata': metadata,
    });
  }

  Future<List<Activity>> listActivities(String accountId) async {
    final response = await _dio.get('/api/users/$accountId/activities');
    return (response.data as List)
        .map((json) => Activity.fromJson(json as Map<String, dynamic>))
        .toList();
  }
}