class Activity {
  final String id;
  final String accountId;
  final String activityType;
  final String occurredAt;
  final String? metadata;

  Activity({
    required this.id,
    required this.accountId,
    required this.activityType,
    required this.occurredAt,
    this.metadata,
  });

  factory Activity.fromJson(Map<String, dynamic> json) {
    return Activity(
      id: json['id'] as String,
      accountId: json['accountId'] as String,
      activityType: json['activityType'] as String,
      occurredAt: json['occurredAt'] as String,
      metadata: json['metadata'] as String?,
    );
  }
}