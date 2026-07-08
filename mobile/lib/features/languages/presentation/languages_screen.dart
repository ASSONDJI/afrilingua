import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'language_providers.dart';

class LanguagesScreen extends ConsumerWidget {
  const LanguagesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final languagesAsync = ref.watch(languagesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Choisis une langue'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person),
            onPressed: () => context.push('/profile'),
          ),
        ],
      ),
      body: languagesAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              "Impossible de charger les langues.\nVérifie que le backend tourne.\n\n$error",
              textAlign: TextAlign.center,
            ),
          ),
        ),
        data: (languages) {
          if (languages.isEmpty) {
            return const Center(child: Text('Aucune langue disponible pour le moment.'));
          }
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: languages.length,
            separatorBuilder: (context, index) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final language = languages[index];
              return Card(
                elevation: 0,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                child: ListTile(
                  contentPadding: const EdgeInsets.all(16),
                  title: Text(
                    language.name,
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
                  ),
                  subtitle: Text('${language.region} · ${language.totalWords} mots'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => context.push('/lessons/${language.id}', extra: language.name),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
