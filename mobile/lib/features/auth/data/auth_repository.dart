import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/auth_models.dart';

class AuthException implements Exception {
  final String message;
  AuthException(this.message);
}

class AuthRepository {
  final Dio _dio;

  AuthRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<AuthResponse> register({required String email, required String password}) async {
    try {
      final response = await _dio.post('/api/auth/register', data: {
        'email': email,
        'password': password,
      });
      return AuthResponse.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AuthException(_extractMessage(e));
    }
  }

  Future<AuthResponse> login({required String email, required String password}) async {
    try {
      final response = await _dio.post('/api/auth/login', data: {
        'email': email,
        'password': password,
      });
      return AuthResponse.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw AuthException(_extractMessage(e));
    }
  }

  String _extractMessage(DioException e) {
    final data = e.response?.data;
    if (data is Map && data['message'] != null) {
      return data['message'] as String;
    }
    return 'Une erreur réseau est survenue. Vérifie ta connexion.';
  }
}
