import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../auth/presentation/auth_providers.dart';
import 'user_providers.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profileAsync = ref.watch(userProfileProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Mon profil')),
      body: profileAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              "Impossible de charger le profil.\n\n$error",
              textAlign: TextAlign.center,
            ),
          ),
        ),
        data: (profile) {
          return Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                CircleAvatar(
                  radius: 40,
                  backgroundColor: Theme.of(context).colorScheme.primary,
                  child: Text(
                    profile.displayName.isNotEmpty ? profile.displayName[0].toUpperCase() : '?',
                    style: const TextStyle(fontSize: 32, color: Colors.white),
                  ),
                ),
                const SizedBox(height: 16),
                Text(profile.displayName, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w700)),
                if (profile.bio != null) ...[
                  const SizedBox(height: 8),
                  Text(profile.bio!, style: Theme.of(context).textTheme.bodyMedium),
                ],
                const SizedBox(height: 32),
                ElevatedButton.icon(
                  onPressed: () => context.push('/progression'),
                  icon: const Icon(Icons.emoji_events_rounded),
                  label: const Text('Ma progression'),
                ),
                const SizedBox(height: 12),
                OutlinedButton.icon(
                  onPressed: () async {
                    await ref.read(authControllerProvider.notifier).logout();
                    if (context.mounted) {
                      // ignore: use_build_context_synchronously
                      Navigator.of(context).popUntil((route) => route.isFirst);
                    }
                  },
                  icon: const Icon(Icons.logout),
                  label: const Text('Se déconnecter'),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}