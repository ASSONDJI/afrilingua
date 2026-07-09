import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../domain/statistics_point.dart';
import '../domain/word_model.dart';

class NotEnoughDataException implements Exception {
  final String message;
  NotEnoughDataException(this.message);
}

class StatisticsRepository {
  final Dio _dio;

  StatisticsRepository(ApiClient apiClient) : _dio = apiClient.dio;

  Future<List<StatisticsPoint>> loadWordStatistics(String languageId) async {
    final wordsResponse = await _dio.get('/api/content/languages/$languageId/words');
    final allWords = (wordsResponse.data as List).cast<Map<String, dynamic>>();

    final annotated = allWords
        .map(WordWithTones.fromJsonIfAnnotated)
        .whereType<WordWithTones>()
        .toList();

    if (annotated.length < 2) {
      throw NotEnoughDataException(
        'Pas assez de mots avec annotation tonale pour cette langue '
        '(${annotated.length} trouvé(s), 2 minimum). Seul le Yemba est '
        'actuellement annoté avec des tons.',
      );
    }

    final wordFeatures = annotated
        .map((w) => {
              'word_id': w.id,
              'nb_syllabes': w.nbSyllabes,
              'longueur_mot': w.word.length,
              'ton1': w.tone1,
              'ton2': w.tone2,
            })
        .toList();

    final nClusters = annotated.length < 8 ? annotated.length : 8;

    final results = await Future.wait([
      _dio.post('/api/recommendations/pca', data: {'words': wordFeatures}),
      _dio.post('/api/recommendations/clustering', data: {
        'words': wordFeatures,
        'n_clusters': nClusters,
      }),
    ]);

    final pcaData = results[0].data as Map<String, dynamic>;
    final clusterData = (results[1].data as List).cast<Map<String, dynamic>>();

    final clusterByWordId = <String, int>{
      for (final entry in clusterData) entry['word_id'] as String: entry['cluster'] as int,
    };
    final wordById = {for (final w in annotated) w.id: w};

    return (pcaData['points'] as List).cast<Map<String, dynamic>>().map((json) {
      final wordId = json['word_id'] as String;
      final word = wordById[wordId]!;
      return StatisticsPoint(
        wordId: wordId,
        word: word.word,
        translation: word.translation,
        x: (json['x'] as num).toDouble(),
        y: (json['y'] as num).toDouble(),
        cluster: clusterByWordId[wordId] ?? 0,
      );
    }).toList();
  }
}
