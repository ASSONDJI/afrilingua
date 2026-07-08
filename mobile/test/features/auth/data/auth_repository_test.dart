import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:afrilingua_app/core/network/api_client.dart';
import 'package:afrilingua_app/features/auth/data/auth_repository.dart';

class MockDio extends Mock implements Dio {}

void main() {
  late MockDio mockDio;
  late AuthRepository repository;

  setUp(() {
    mockDio = MockDio();
    repository = AuthRepository(ApiClient.withDio(mockDio));
  });

  group('login', () {
    test('parses a successful auth response', () async {
      when(() => mockDio.post('/api/auth/login', data: any(named: 'data')))
          .thenAnswer((_) async => Response(
                requestOptions: RequestOptions(path: '/api/auth/login'),
                data: {
                  'id': 'account-1',
                  'accessToken': 'access-token',
                  'refreshToken': 'refresh-token',
                  'email': 'malaika@afrilingua.cm',
                  'role': 'USER',
                },
                statusCode: 200,
              ));

      final result = await repository.login(
        email: 'malaika@afrilingua.cm',
        password: 'password123',
      );

      expect(result.id, 'account-1');
      expect(result.accessToken, 'access-token');
    });

    test('throws AuthException with the backend message on 401', () async {
      when(() => mockDio.post('/api/auth/login', data: any(named: 'data')))
          .thenThrow(DioException(
        requestOptions: RequestOptions(path: '/api/auth/login'),
        response: Response(
          requestOptions: RequestOptions(path: '/api/auth/login'),
          statusCode: 401,
          data: {'message': 'Invalid credentials'},
        ),
      ));

      expect(
        () => repository.login(email: 'wrong@afrilingua.cm', password: 'wrong'),
        throwsA(isA<AuthException>().having(
          (e) => e.message,
          'message',
          'Invalid credentials',
        )),
      );
    });

    test('falls back to a generic message when the backend gives none', () async {
      when(() => mockDio.post('/api/auth/login', data: any(named: 'data')))
          .thenThrow(DioException(
        requestOptions: RequestOptions(path: '/api/auth/login'),
      ));

      expect(
        () => repository.login(email: 'malaika@afrilingua.cm', password: 'password123'),
        throwsA(isA<AuthException>().having(
          (e) => e.message,
          'message',
          'Une erreur réseau est survenue. Vérifie ta connexion.',
        )),
      );
    });
  });
}