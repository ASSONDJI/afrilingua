class WordWithTones {
  final String id;
  final String word;
  final String translation;
  final int nbSyllabes;
  final String tone1;
  final String tone2;

  WordWithTones({
    required this.id,
    required this.word,
    required this.translation,
    required this.nbSyllabes,
    required this.tone1,
    required this.tone2,
  });

  /// Returns null if the word lacks tone annotation — recommendation-service's
  /// PCA/clustering require nbSyllabes + tone1 + tone2, which content-service
  /// only populates for tonal languages like Yemba (nullable fields, see
  /// content-service Word schema).
  static WordWithTones? fromJsonIfAnnotated(Map<String, dynamic> json) {
    final nbSyllabes = json['nbSyllabes'] as int?;
    final tone1 = json['tone1'] as String?;
    final tone2 = json['tone2'] as String?;
    if (nbSyllabes == null || tone1 == null || tone2 == null) {
      return null;
    }
    return WordWithTones(
      id: json['id'] as String,
      word: json['word'] as String,
      translation: json['translation'] as String,
      nbSyllabes: nbSyllabes,
      tone1: tone1,
      tone2: tone2,
    );
  }
}
