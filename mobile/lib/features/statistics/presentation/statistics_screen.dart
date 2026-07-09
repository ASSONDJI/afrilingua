import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/statistics_repository.dart';
import '../domain/statistics_point.dart';
import 'statistics_providers.dart';

class StatisticsScreen extends ConsumerWidget {
  final String languageId;
  final String? languageName;

  const StatisticsScreen({super.key, required this.languageId, this.languageName});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final statsAsync = ref.watch(wordStatisticsProvider(languageId));

    return Scaffold(
      appBar: AppBar(title: Text('Statistiques · ${languageName ?? ""}')),
      body: statsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) {
          final message = error is NotEnoughDataException
              ? error.message
              : 'Impossible de charger les statistiques.\n\n$error';
          return Center(
            child: Padding(padding: const EdgeInsets.all(24), child: Text(message, textAlign: TextAlign.center)),
          );
        },
        data: (points) => _StatisticsBody(points: points),
      ),
    );
  }
}

class _StatisticsBody extends StatefulWidget {
  final List<StatisticsPoint> points;
  const _StatisticsBody({required this.points});

  @override
  State<_StatisticsBody> createState() => _StatisticsBodyState();
}

class _StatisticsBodyState extends State<_StatisticsBody> {
  StatisticsPoint? _selected;

  static const _palette = [
    Color(0xFF1E7A4C), Color(0xFFF5A623), Color(0xFF4A90D9), Color(0xFFD0021B),
    Color(0xFF9013FE), Color(0xFF50E3C2), Color(0xFFB8860B), Color(0xFF7C7C7C),
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const Padding(
          padding: EdgeInsets.all(16),
          child: Text(
            'Projection ACP du vocabulaire, coloré par regroupement K-Means. '
            'Les mots proches partagent des caractéristiques tonales similaires.',
            style: TextStyle(fontSize: 13, color: Colors.black54),
          ),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: GestureDetector(
              onTapUp: _handleTap,
              child: CustomPaint(
                painter: _ScatterPainter(points: widget.points, palette: _palette),
                size: Size.infinite,
              ),
            ),
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(16),
          child: _selected == null
              ? const Text('Touche un point pour voir le mot correspondant.')
              : Card(
                  child: ListTile(
                    title: Text(_selected!.word, style: const TextStyle(fontWeight: FontWeight.w700)),
                    subtitle: Text(_selected!.translation),
                    trailing: Chip(
                      label: Text('Groupe ${_selected!.cluster + 1}'),
                      backgroundColor: _palette[_selected!.cluster % _palette.length].withValues(alpha: 0.2),
                    ),
                  ),
                ),
        ),
      ],
    );
  }

  void _handleTap(TapUpDetails details) {
    final box = context.findRenderObject() as RenderBox?;
    if (box == null || widget.points.isEmpty) return;

    final size = box.size;
    final (minX, maxX, minY, maxY) = _bounds(widget.points);
    const padding = 24.0;

    StatisticsPoint? closest;
    double closestDistance = double.infinity;

    for (final point in widget.points) {
      final offset = _project(point, minX, maxX, minY, maxY, size, padding);
      final distance = (details.localPosition - offset).distance;
      if (distance < closestDistance) {
        closestDistance = distance;
        closest = point;
      }
    }

    if (closest != null && closestDistance < 30) {
      setState(() => _selected = closest);
    }
  }
}

(double, double, double, double) _bounds(List<StatisticsPoint> points) {
  final xs = points.map((p) => p.x);
  final ys = points.map((p) => p.y);
  return (xs.reduce((a, b) => a < b ? a : b), xs.reduce((a, b) => a > b ? a : b),
      ys.reduce((a, b) => a < b ? a : b), ys.reduce((a, b) => a > b ? a : b));
}

Offset _project(StatisticsPoint point, double minX, double maxX, double minY, double maxY, Size size, double padding) {
  final dx = padding + (maxX == minX ? 0.5 : (point.x - minX) / (maxX - minX)) * (size.width - 2 * padding);
  final dy = size.height - padding - (maxY == minY ? 0.5 : (point.y - minY) / (maxY - minY)) * (size.height - 2 * padding);
  return Offset(dx, dy);
}

class _ScatterPainter extends CustomPainter {
  final List<StatisticsPoint> points;
  final List<Color> palette;
  _ScatterPainter({required this.points, required this.palette});

  @override
  void paint(Canvas canvas, Size size) {
    if (points.isEmpty) return;
    final (minX, maxX, minY, maxY) = _bounds(points);
    const padding = 24.0;

    for (final point in points) {
      final offset = _project(point, minX, maxX, minY, maxY, size, padding);
      final paint = Paint()..color = palette[point.cluster % palette.length];
      canvas.drawCircle(offset, 6, paint);
    }
  }

  @override
  bool shouldRepaint(covariant _ScatterPainter oldDelegate) => oldDelegate.points != points;
}
