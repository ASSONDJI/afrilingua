import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/network/api_client.dart';
import '../data/auth_repository.dart';
import '../domain/auth_models.dart';

final apiClientProvider = Provider((ref) => ApiClient(ref.watch(secureStorageProvider)));

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
    await _persistSession(result);
    state = result;
    return result;
  }

  Future<AuthResponse> register(String email, String password) async {
    final result = await _repository.register(email: email, password: password);
    await _persistSession(result);
    state = result;
    return result;
  }

  Future<void> _persistSession(AuthResponse result) async {
    await _storage.write(key: 'access_token', value: result.accessToken);
    await _storage.write(key: 'refresh_token', value: result.refreshToken);
    await _storage.write(key: 'account_id', value: result.id);
    await _storage.write(key: 'email', value: result.email);
    await _storage.write(key: 'role', value: result.role);
  }

  /// Reconstructs the session from secure storage on app startup (page
  /// reload, direct URL navigation, hot restart). Without this, the
  /// in-memory-only state means every reload silently logs the user out
  /// even though valid tokens are still on disk.
  Future<void> restoreSession() async {
    final accessToken = await _storage.read(key: 'access_token');
    final refreshToken = await _storage.read(key: 'refresh_token');
    final accountId = await _storage.read(key: 'account_id');
    final email = await _storage.read(key: 'email');
    final role = await _storage.read(key: 'role');

    if (accessToken != null && refreshToken != null && accountId != null && email != null && role != null) {
      state = AuthResponse(
        id: accountId,
        accessToken: accessToken,
        refreshToken: refreshToken,
        email: email,
        role: role,
      );
    }
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
/// Gate used at app startup: the app waits for this to complete before
/// building any real screen, so no route ever runs with a stale/null
/// session while restoreSession() is still in flight.
final sessionRestorationProvider = FutureProvider<void>((ref) async {
  await ref.read(authControllerProvider.notifier).restoreSession();
});
