import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:afrilingua_app/core/network/api_client.dart';
import 'package:afrilingua_app/features/quiz/data/quiz_repository.dart';

class MockDio extends Mock implements Dio {}

void main() {
  late MockDio mockDio;
  late QuizRepository repository;

  setUpAll(() {
    registerFallbackValue(Options());
  });

  setUp(() {
    mockDio = MockDio();
    repository = QuizRepository(ApiClient.withDio(mockDio));
  });

  group('listByLesson', () {
    test('parses a list of questions from the response body', () async {
      when(() => mockDio.get(
            '/api/quizzes',
            queryParameters: any(named: 'queryParameters'),
          )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/quizzes'),
            data: [
              {
                'id': 'q1',
                'lessonId': 'lesson1',
                'type': 'MULTIPLE_CHOICE',
                'questionText': 'Comment dit-on maison ?',
                'options': ['nsem', 'tsa'],
              },
            ],
            statusCode: 200,
          ));

      final questions = await repository.listByLesson('lesson1');

      expect(questions, hasLength(1));
      expect(questions.first.questionText, 'Comment dit-on maison ?');
    });
  });

  group('submitAnswer', () {
    test('parses the answer result from the response body', () async {
      when(() => mockDio.post(
            '/api/quizzes/q1/submit',
            data: any(named: 'data'),
          )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/quizzes/q1/submit'),
            data: {
              'questionId': 'q1',
              'correct': true,
              'correctAnswer': 'nsem',
            },
            statusCode: 200,
          ));

      final result = await repository.submitAnswer('q1', 'nsem');

      expect(result.correct, isTrue);
      expect(result.correctAnswer, 'nsem');
    });
  });
}