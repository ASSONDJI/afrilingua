import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/language_repository.dart';
import '../domain/language_model.dart';

final languageRepositoryProvider = Provider((ref) {
  return LanguageRepository(ref.watch(apiClientProvider));
});

final languagesProvider = FutureProvider<List<Language>>((ref) async {
  return ref.watch(languageRepositoryProvider).listAll();
});
