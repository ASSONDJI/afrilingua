import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'lesson_providers.dart';

class LessonsScreen extends ConsumerWidget {
  final String languageId;
  final String? languageName;

  const LessonsScreen({super.key, required this.languageId, this.languageName});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final lessonsAsync = ref.watch(lessonsByLanguageProvider(languageId));

    return Scaffold(
      appBar: AppBar(title: Text(languageName ?? 'Leçons')),
      body: lessonsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text('Impossible de charger les leçons.\n\n$error', textAlign: TextAlign.center),
          ),
        ),
        data: (lessons) {
          if (lessons.isEmpty) {
            return const Center(child: Text('Aucune leçon disponible pour cette langue pour le moment.'));
          }
          final sorted = [...lessons]..sort((a, b) => a.order.compareTo(b.order));
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: sorted.length,
           separatorBuilder: (context, index) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final lesson = sorted[index];
              return Card(
                elevation: 0,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                child: ListTile(
                  contentPadding: const EdgeInsets.all(16),
                  leading: CircleAvatar(
                    backgroundColor: Theme.of(context).colorScheme.primary,
                    foregroundColor: Colors.white,
                    child: Text('${lesson.order}'),
                  ),
                  title: Text(lesson.title, style: const TextStyle(fontWeight: FontWeight.w700)),
                  subtitle: Text('Niveau ${lesson.level} · ${lesson.wordIds.length} mots'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {
                    // Prochaine étape : navigation vers l'écran quiz de cette leçon
                  },
                ),
              );
            },
          );
        },
      ),
    );
  }
}
