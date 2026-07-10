#!/usr/bin/env python3
"""
Genere des lecons a partir du vocabulaire deja importe dans content-service,
en regroupant les mots par niveau de difficulte deja calcule (classification
C4.5 automatique, voir WordService.resolveDifficultyLevel) plutot que par un
critere arbitraire.

Pour chaque langue : les mots BEGINNER puis INTERMEDIATE puis ADVANCED sont
decoupes en lecons de taille fixe (par defaut 9 mots), dans cet ordre --
progression pedagogique naturelle du plus facile au plus difficile.

Idempotent par langue : si une langue a deja au moins une lecon, elle est
ignoree par defaut (pour ne pas dupliquer du contenu existant comme la lecon
"Salutations" deja creee pour Yemba). Utiliser --force pour regenerer quand
meme (les lecons existantes ne sont pas supprimees, de nouvelles s'ajoutent
a la suite avec un ordre continu).

Usage:
    python3 generate_lessons.py --words-per-lesson 9
    python3 generate_lessons.py --language-id <uuid> --force
"""
import argparse

import requests

CONTENT_BASE_URL = "http://localhost:8080/api/content"
LESSON_BASE_URL = "http://localhost:8080/api"

DIFFICULTY_ORDER = ["BEGINNER", "INTERMEDIATE", "ADVANCED"]
DIFFICULTY_LABELS = {
    "BEGINNER": "Débutant",
    "INTERMEDIATE": "Intermédiaire",
    "ADVANCED": "Avancé",
}
DIFFICULTY_LEVEL_NUMBER = {"BEGINNER": 1, "INTERMEDIATE": 2, "ADVANCED": 3}


def chunk(items: list, size: int) -> list[list]:
    return [items[i : i + size] for i in range(0, len(items), size)]


def fetch_languages() -> list[dict]:
    response = requests.get(f"{CONTENT_BASE_URL}/languages", timeout=10)
    response.raise_for_status()
    return response.json()


def fetch_words(language_id: str) -> list[dict]:
    response = requests.get(f"{CONTENT_BASE_URL}/languages/{language_id}/words", timeout=10)
    response.raise_for_status()
    return response.json()


def fetch_existing_lesson_count(language_id: str) -> int:
    response = requests.get(f"{LESSON_BASE_URL}/lessons", params={"languageId": language_id}, timeout=10)
    response.raise_for_status()
    return len(response.json())


def create_lesson(language_id: str, title: str, order: int, level: int) -> str:
    payload = {"languageId": language_id, "title": title, "order": order, "level": level}
    response = requests.post(f"{LESSON_BASE_URL}/lessons", json=payload, timeout=10)
    response.raise_for_status()
    return response.json()["id"]


def attach_word(lesson_id: str, word_id: str) -> None:
    response = requests.post(
        f"{LESSON_BASE_URL}/lessons/{lesson_id}/words", json={"wordId": word_id}, timeout=10
    )
    response.raise_for_status()


def generate_for_language(language: dict, words_per_lesson: int, force: bool) -> None:
    language_id = language["id"]
    language_name = language["name"]

    existing_count = fetch_existing_lesson_count(language_id)
    if existing_count > 0 and not force:
        print(f"[{language_name}] {existing_count} lecon(s) deja presente(s), ignore (utiliser --force pour regenerer)")
        return

    words = fetch_words(language_id)
    if not words:
        print(f"[{language_name}] Aucun mot en base, rien a generer")
        return

    words_by_level = {level: [] for level in DIFFICULTY_ORDER}
    for word in words:
        level = word.get("difficultyLevel")
        if level in words_by_level:
            words_by_level[level].append(word)

    order_counter = existing_count + 1
    lessons_created = 0
    words_attached = 0

    for level in DIFFICULTY_ORDER:
        level_words = words_by_level[level]
        if not level_words:
            continue

        chunks = chunk(level_words, words_per_lesson)
        for part_number, word_chunk in enumerate(chunks, start=1):
            title = f"{language_name} · {DIFFICULTY_LABELS[level]} · Partie {part_number}"
            lesson_id = create_lesson(
                language_id=language_id,
                title=title,
                order=order_counter,
                level=DIFFICULTY_LEVEL_NUMBER[level],
            )
            for word in word_chunk:
                attach_word(lesson_id, word["id"])
                words_attached += 1

            lessons_created += 1
            order_counter += 1

    print(f"[{language_name}] {lessons_created} lecon(s) creee(s), {words_attached} mot(s) attache(s)")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--words-per-lesson", type=int, default=9)
    parser.add_argument("--language-id", help="Ne traiter qu'une seule langue (sinon toutes)")
    parser.add_argument("--force", action="store_true", help="Regenerer meme si des lecons existent deja")
    args = parser.parse_args()

    languages = fetch_languages()
    if args.language_id:
        languages = [lang for lang in languages if lang["id"] == args.language_id]
        if not languages:
            print(f"Langue {args.language_id!r} introuvable")
            return

    for language in languages:
        generate_for_language(language, args.words_per_lesson, args.force)


if __name__ == "__main__":
    main()
