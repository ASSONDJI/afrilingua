class StatisticsPoint {
  final String wordId;
  final String word;
  final String translation;
  final double x;
  final double y;
  final int cluster;

  StatisticsPoint({
    required this.wordId,
    required this.word,
    required this.translation,
    required this.x,
    required this.y,
    required this.cluster,
  });
}
