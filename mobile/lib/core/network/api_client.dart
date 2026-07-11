import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'auth_interceptor.dart';

/// Single entry point for all HTTP calls, routed through api-gateway.
/// Never hit a microservice's own port directly from the app.
class ApiClient {
  static const String baseUrl = 'http://localhost:8080';
  late final Dio dio;

  ApiClient(FlutterSecureStorage storage) {
    dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10),
        headers: {'Content-Type': 'application/json'},
      ),
    );
    dio.interceptors.add(AuthInterceptor(storage));
  }

  /// Test-only constructor: injects a (typically mocked) Dio instance
  /// directly, bypassing real network configuration and the auth interceptor.
  ApiClient.withDio(this.dio);
}
