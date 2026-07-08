class UserPreferences {
  final String accountId;
  final List<String> learningLanguages;

  UserPreferences({
    required this.accountId,
    required this.learningLanguages,
  });

  factory UserPreferences.fromJson(Map<String, dynamic> json) {
    return UserPreferences(
      accountId: json['accountId'] as String,
      learningLanguages: (json['learningLanguages'] as List<dynamic>? ?? []).cast<String>(),
    );
  }
}