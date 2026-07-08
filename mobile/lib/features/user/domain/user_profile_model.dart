class UserProfile {
  final String id;
  final String accountId;
  final String displayName;
  final String? avatarUrl;
  final String? bio;

  UserProfile({
    required this.id,
    required this.accountId,
    required this.displayName,
    this.avatarUrl,
    this.bio,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      displayName: json['displayName'] as String,
      avatarUrl: json['avatarUrl'] as String?,
      bio: json['bio'] as String?,
    );
  }
}