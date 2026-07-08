import 'package:flutter_test/flutter_test.dart';
import 'package:afrilingua_app/features/auth/domain/auth_models.dart';

void main() {
  group('AuthResponse.fromJson', () {
    test('parses a complete auth response, including id', () {
      final json = {
        'id': 'ca21249f-65e9-4c75-9957-551ad9e5a69f',
        'accessToken': 'access-token',
        'refreshToken': 'refresh-token',
        'email': 'malaika@afrilingua.cm',
        'role': 'USER',
      };

      final response = AuthResponse.fromJson(json);

      expect(response.id, 'ca21249f-65e9-4c75-9957-551ad9e5a69f');
      expect(response.accessToken, 'access-token');
      expect(response.refreshToken, 'refresh-token');
      expect(response.email, 'malaika@afrilingua.cm');
      expect(response.role, 'USER');
    });
  });
}
