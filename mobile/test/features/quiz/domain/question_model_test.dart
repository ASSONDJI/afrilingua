import 'package:flutter_test/flutter_test.dart';
import 'package:afrilingua_app/features/quiz/domain/question_model.dart';

void main() {
  group('Question.fromJson', () {
    test('parses a complete JSON response', () {
      final json = {
        'id': 'd218d37c-0a4b-458d-b78c-9fc6ff37ee1d',
        'lessonId': '2de111d3-69ab-4a40-bbf8-ee23f490bcae',
        'type': 'MULTIPLE_CHOICE',
        'questionText': 'Comment dit-on maison en yemba ?',
        'options': ['nsem', 'tsa', 'ndap'],
      };

      final question = Question.fromJson(json);

      expect(question.id, 'd218d37c-0a4b-458d-b78c-9fc6ff37ee1d');
      expect(question.lessonId, '2de111d3-69ab-4a40-bbf8-ee23f490bcae');
      expect(question.type, 'MULTIPLE_CHOICE');
      expect(question.questionText, 'Comment dit-on maison en yemba ?');
      expect(question.options, ['nsem', 'tsa', 'ndap']);
    });

    test('defaults options to an empty list when missing', () {
      final json = {
        'id': 'd218d37c-0a4b-458d-b78c-9fc6ff37ee1d',
        'lessonId': '2de111d3-69ab-4a40-bbf8-ee23f490bcae',
        'type': 'FILL_IN_THE_BLANK',
        'questionText': 'Complète la phrase',
      };

      final question = Question.fromJson(json);

      expect(question.options, isEmpty);
    });
  });
}
