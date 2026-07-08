import 'package:dio/dio.dart';

/// Single entry point for all HTTP calls, routed through api-gateway.
/// Never hit a microservice's own port directly from the app.
class ApiClient {
  static const String baseUrl = 'http://localhost:8080';
  late final Dio dio;

  ApiClient() {
    dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10),
        headers: {'Content-Type': 'application/json'},
      ),
    );
  }

  /// Test-only constructor: injects a (typically mocked) Dio instance
  /// directly, bypassing real network configuration.
  ApiClient.withDio(this.dio);
}