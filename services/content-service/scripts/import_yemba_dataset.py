#!/usr/bin/env python3
"""
Import du dataset YembaTones (mots isoles + tons) vers content-service, via
l'API REST existante -- reutilise la classification automatique de difficulte
(recommendation-service, C4.5 sur les tons) deja en place dans
WordService.resolveDifficultyLevel, sans dupliquer cette logique ici.

Le dataset original (non commite, fichier volumineux avec audio) contient
aussi des enregistrements de comparaison tonale (statement_XvsY) utilises pour
une etude de perception -- hors perimetre pedagogique. Seul le tableau de mots
isoles (colonnes WordId, Yemba, French, tons) est importe ici. L'audio n'est
pas importe pour l'instant : pas de mecanisme d'upload cote content-service
aujourd'hui (Media Service prevu en phase 2, cahier des charges §2.3).

Usage:
    python3 import_yemba_dataset.py --xlsx /path/to/isolated_words_dictionary.xlsx \\
        --language-id a3f52af1-03d5-4ba0-91a6-c10d2ce1b468

Idempotent : les mots deja presents pour cette langue (meme orthographe,
insensible a la casse) sont ignores plutot que dupliques -- rejouable sans
risque si le script est relance apres un import partiel.
"""
import argparse
import sys
import time

import pandas as pd
import requests

TONE_MAP = {"bas": "bas", "moyen": "moyen", "haut": "haut"}


def normalize_tone(value) -> str | None:
    if pd.isna(value):
        return None
    return TONE_MAP.get(str(value).strip().lower())


def count_syllables(row) -> int:
    return sum(1 for col in ("Syllabe 1", "Syllabe 2", "Syllabe 3") if pd.notna(row.get(col)))


def fetch_existing_words(base_url: str, language_id: str) -> set[str]:
    response = requests.get(f"{base_url}/languages/{language_id}/words", timeout=10)
    response.raise_for_status()
    return {w["word"].strip().lower() for w in response.json()}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--xlsx", required=True, help="Chemin vers isolated_words_dictionary.xlsx")
    parser.add_argument("--language-id", required=True, help="UUID de la langue Yemba dans content-service")
    parser.add_argument(
        "--base-url",
        default="http://localhost:8080/api/content",
        help="URL de base de content-service (via api-gateway par defaut)",
    )
    args = parser.parse_args()

    df = pd.read_excel(args.xlsx)
    print(f"Dataset charge : {len(df)} mots")

    existing = fetch_existing_words(args.base_url, args.language_id)
    print(f"Mots deja presents pour cette langue : {len(existing)}")

    imported, skipped, failed = 0, 0, 0
    failures = []

    for _, row in df.iterrows():
        word = str(row["Yemba"]).strip()

        if word.lower() in existing:
            skipped += 1
            continue

        payload = {
            "word": word,
            "translation": str(row["French"]).strip(),
            "grammaticalCategory": None,
            "nbSyllabes": count_syllables(row) or None,
            "tone1": normalize_tone(row.get("Tone 1")),
            "tone2": normalize_tone(row.get("Tone 2")),
        }

        try:
            response = requests.post(
                f"{args.base_url}/languages/{args.language_id}/words",
                json=payload,
                timeout=10,
            )
            if response.status_code == 201:
                imported += 1
                existing.add(word.lower())
            else:
                failed += 1
                failures.append((word, response.status_code, response.text[:200]))
        except requests.RequestException as exc:
            failed += 1
            failures.append((word, "EXCEPTION", str(exc)))

        # Petite pause : chaque mot avec tons declenche un appel synchrone
        # vers recommendation-service (classification C4.5).
        time.sleep(0.02)

    print(f"\nImportes : {imported}")
    print(f"Ignores (deja presents) : {skipped}")
    print(f"Echoues  : {failed}")

    if failures:
        print("\nDetail des echecs (20 premiers) :")
        for word, status, detail in failures[:20]:
            print(f"  - {word!r} -> {status} : {detail}")
        sys.exit(1)


if __name__ == "__main__":
    main()
