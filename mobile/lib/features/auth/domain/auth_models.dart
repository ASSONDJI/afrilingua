class AuthResponse {
  final String accessToken;
  final String refreshToken;
  final String email;
  final String role;

  AuthResponse({
    required this.accessToken,
    required this.refreshToken,
    required this.email,
    required this.role,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      email: json['email'] as String,
      role: json['role'] as String,
    );
  }
}
