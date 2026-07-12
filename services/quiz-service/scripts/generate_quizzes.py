#!/usr/bin/env python3
"""
Genere des questions QCM (MULTIPLE_CHOICE) pour chaque lecon deja creee,
a partir des mots qui lui sont attaches (content-service + lesson-service).

Pour chaque mot d'une lecon : une question "Comment dit-on {traduction} en
{langue} ?", avec la bonne reponse (le mot) melangee a 3 distracteurs tires
aleatoirement parmi les AUTRES mots de la MEME lecon (des choix plausibles,
du meme registre/niveau, plutot que des mots au hasard dans toute la langue).

Une lecon avec moins de 4 mots ne peut pas avoir de distracteurs suffisants :
elle est alors ignoree avec un avertissement plutot que de generer une
question avec moins de 4 options.

Idempotent par lecon : une lecon ayant deja au moins une question est ignoree
par defaut (meme logique que generate_lessons.py). Utiliser --force pour
regenerer (les questions existantes ne sont pas supprimees).

Usage:
    python3 generate_quizzes.py
    python3 generate_quizzes.py --language-id <uuid> --force
"""
import argparse
import random

import requests

CONTENT_BASE_URL = "http://localhost:8080/api/content"
LESSON_BASE_URL = "http://localhost:8080/api"
QUIZ_BASE_URL = "http://localhost:8080/api"

MIN_WORDS_FOR_QUESTION = 4  # 1 bonne reponse + 3 distracteurs


def fetch_languages() -> list[dict]:
    response = requests.get(f"{CONTENT_BASE_URL}/languages", timeout=10)
    response.raise_for_status()
    return response.json()


def fetch_lessons(language_id: str) -> list[dict]:
    response = requests.get(f"{LESSON_BASE_URL}/lessons", params={"languageId": language_id}, timeout=10)
    response.raise_for_status()
    return response.json()


def fetch_word(word_id: str) -> dict:
    response = requests.get(f"{CONTENT_BASE_URL}/words/{word_id}", timeout=10)
    response.raise_for_status()
    return response.json()


def fetch_existing_question_count(lesson_id: str) -> int:
    response = requests.get(f"{QUIZ_BASE_URL}/quizzes", params={"lessonId": lesson_id}, timeout=10)
    response.raise_for_status()
    return len(response.json())


def create_question(lesson_id: str, word_id: str, question_text: str, options: list[str], correct_answer: str) -> None:
    payload = {
        "lessonId": lesson_id,
        "wordId": word_id,
        "type": "MULTIPLE_CHOICE",
        "questionText": question_text,
        "options": options,
        "correctAnswer": correct_answer,
    }
    response = requests.post(f"{QUIZ_BASE_URL}/quizzes", json=payload, timeout=10)
    response.raise_for_status()


def generate_for_lesson(language_name: str, lesson: dict, force: bool) -> tuple[int, int]:
    lesson_id = lesson["id"]
    word_ids = lesson.get("wordIds", [])

    if len(word_ids) < MIN_WORDS_FOR_QUESTION:
        print(f"  [{lesson['title']}] Ignoree : seulement {len(word_ids)} mot(s), minimum {MIN_WORDS_FOR_QUESTION} requis")
        return 0, 0

    existing_count = fetch_existing_question_count(lesson_id)
    if existing_count > 0 and not force:
        print(f"  [{lesson['title']}] {existing_count} question(s) deja presente(s), ignoree")
        return 0, 0

    words = [fetch_word(wid) for wid in word_ids]
    questions_created = 0

    for word in words:
        distractor_pool = [w["word"] for w in words if w["id"] != word["id"]]
        distractors = random.sample(distractor_pool, min(3, len(distractor_pool)))
        options = distractors + [word["word"]]
        random.shuffle(options)

        question_text = f"Comment dit-on « {word['translation']} » en {language_name} ?"
        create_question(lesson_id, word["id"], question_text, options, word["word"])
        questions_created += 1

    print(f"  [{lesson['title']}] {questions_created} question(s) creee(s)")
    return questions_created, 1


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--language-id", help="Ne traiter qu'une seule langue (sinon toutes)")
    parser.add_argument("--force", action="store_true", help="Regenerer meme si des questions existent deja")
    args = parser.parse_args()

    languages = fetch_languages()
    if args.language_id:
        languages = [lang for lang in languages if lang["id"] == args.language_id]
        if not languages:
            print(f"Langue {args.language_id!r} introuvable")
            return

    for language in languages:
        print(f"[{language['name']}]")
        lessons = fetch_lessons(language["id"])
        total_questions, total_lessons = 0, 0
        for lesson in lessons:
            q, l = generate_for_lesson(language["name"], lesson, args.force)
            total_questions += q
            total_lessons += l
        print(f"  -> {total_lessons} lecon(s) traitee(s), {total_questions} question(s) creee(s) au total\n")


if __name__ == "__main__":
    main()
