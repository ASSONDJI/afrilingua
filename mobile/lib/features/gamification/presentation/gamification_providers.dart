import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/gamification_repository.dart';
import '../domain/badge_catalog_model.dart';
import '../domain/progression_model.dart';

final gamificationRepositoryProvider = Provider((ref) {
  return GamificationRepository(ref.watch(apiClientProvider));
});

/// Autodispose : la progression doit se rafraichir a chaque entree sur
/// l'ecran plutot que de rester en cache indefiniment, puisque l'XP change
/// frequemment (a chaque lecon/quiz termine ailleurs dans l'app).
final progressionProvider =
    FutureProvider.autoDispose<Progression>((ref) async {
  final userId = ref.watch(accountIdProvider);
  if (userId == null) {
    throw StateError('Aucun utilisateur connecte');
  }
  return ref.watch(gamificationRepositoryProvider).loadProgression(userId);
});

/// Le catalogue est statique cote backend (seede via migration), donc pas
/// besoin d'autoDispose ici : une fois charge, il reste valide pour toute
/// la session.
final badgeCatalogProvider = FutureProvider<List<BadgeCatalogEntry>>((ref) async {
  return ref.watch(gamificationRepositoryProvider).loadBadgeCatalog();
});

/// Combine progression + catalogue en un seul etat de chargement, pour que
/// l'ecran n'ait pas a synchroniser deux AsyncValue independants (loading/
/// error de l'un pouvant arriver avant l'autre).
final gamificationDataProvider =
    FutureProvider.autoDispose<(Progression, List<BadgeCatalogEntry>)>((ref) async {
  final results = await Future.wait([
    ref.watch(progressionProvider.future),
    ref.watch(badgeCatalogProvider.future),
  ]);
  return (results[0] as Progression, results[1] as List<BadgeCatalogEntry>);
});
