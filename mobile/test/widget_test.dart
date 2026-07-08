import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:afrilingua_app/main.dart';

void main() {
  testWidgets('App starts and shows the login screen', (WidgetTester tester) async {
    await tester.pumpWidget(const ProviderScope(child: AfriLinguaApp()));
    await tester.pumpAndSettle();
    expect(find.text('AfriLingua'), findsOneWidget);
    expect(find.text('Se connecter'), findsOneWidget);
  });
}
