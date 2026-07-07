class Language {
  final String id;
  final String name;
  final String code;
  final String region;
  final int totalWords;

  Language({
    required this.id,
    required this.name,
    required this.code,
    required this.region,
    required this.totalWords,
  });

  factory Language.fromJson(Map<String, dynamic> json) {
    return Language(
      id: json['id'] as String,
      name: json['name'] as String,
      code: json['code'] as String,
      region: json['region'] as String,
      totalWords: json['totalWords'] as int,
    );
  }
}
