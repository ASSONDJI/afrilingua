class BadgeCatalogEntry {
  final String code;
  final String name;
  final String description;
  final String criteriaType;
  final int criteriaValue;

  BadgeCatalogEntry({
    required this.code,
    required this.name,
    required this.description,
    required this.criteriaType,
    required this.criteriaValue,
  });

  factory BadgeCatalogEntry.fromJson(Map<String, dynamic> json) {
    return BadgeCatalogEntry(
      code: json['code'] as String,
      name: json['name'] as String,
      description: json['description'] as String,
      criteriaType: json['criteria_type'] as String,
      criteriaValue: json['criteria_value'] as int,
    );
  }
}
