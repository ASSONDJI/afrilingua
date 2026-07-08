import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/network/api_client.dart';
import '../data/auth_repository.dart';
import '../domain/auth_models.dart';

final apiClientProvider = Provider((ref) => ApiClient());

final authRepositoryProvider = Provider((ref) {
  return AuthRepository(ref.watch(apiClientProvider));
});

final secureStorageProvider = Provider((ref) => const FlutterSecureStorage());

/// Holds the current session in memory. Null means "logged out".
class AuthController extends StateNotifier<AuthResponse?> {
  final AuthRepository _repository;
  final FlutterSecureStorage _storage;

  AuthController(this._repository, this._storage) : super(null);

  Future<AuthResponse> login(String email, String password) async {
    final result = await _repository.login(email: email, password: password);
    await _storage.write(key: 'access_token', value: result.accessToken);
    await _storage.write(key: 'refresh_token', value: result.refreshToken);
    await _storage.write(key: 'account_id', value: result.id);
    state = result;
    return result;
  }

  Future<AuthResponse> register(String email, String password) async {
    final result = await _repository.register(email: email, password: password);
    await _storage.write(key: 'access_token', value: result.accessToken);
    await _storage.write(key: 'refresh_token', value: result.refreshToken);
    await _storage.write(key: 'account_id', value: result.id);
    state = result;
    return result;
  }

  Future<void> logout() async {
    await _storage.deleteAll();
    state = null;
  }
}

final authControllerProvider = StateNotifierProvider<AuthController, AuthResponse?>((ref) {
  return AuthController(ref.watch(authRepositoryProvider), ref.watch(secureStorageProvider));
});

/// Convenience accessor: the current session's accountId, or null if logged out.
final accountIdProvider = Provider<String?>((ref) {
  return ref.watch(authControllerProvider)?.id;
});