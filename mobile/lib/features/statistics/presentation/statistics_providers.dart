import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/statistics_repository.dart';
import '../domain/statistics_point.dart';

final statisticsRepositoryProvider = Provider((ref) {
  return StatisticsRepository(ref.watch(apiClientProvider));
});

final wordStatisticsProvider =
    FutureProvider.family<List<StatisticsPoint>, String>((ref, languageId) async {
  return ref.watch(statisticsRepositoryProvider).loadWordStatistics(languageId);
});
