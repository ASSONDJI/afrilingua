import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/user_repository.dart';
import '../domain/user_profile_model.dart';

final userRepositoryProvider = Provider((ref) {
  return UserRepository(ref.watch(apiClientProvider));
});

final userProfileProvider = FutureProvider<UserProfile>((ref) async {
  final accountId = ref.watch(accountIdProvider);
  if (accountId == null) {
    throw StateError('No active session');
  }
  return ref.watch(userRepositoryProvider).getProfile(accountId);
});