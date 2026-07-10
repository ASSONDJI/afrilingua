# Scripts d'import de contenu

## import_yemba_dataset.py

Importe le vocabulaire Yemba (mots + tons) depuis le dataset YembaTones
(source du projet YembaLearn, voir cahier des charges §5) vers content-service.

Le dataset lui-même n'est pas versionné dans ce dépôt (fichier volumineux
avec audio). Il doit être obtenu séparément puis fourni via `--xlsx`.

Dépendances : `pip install pandas openpyxl requests`

Usage :
```bash
python3 import_yemba_dataset.py \
  --xlsx /chemin/vers/isolated_words_dictionary.xlsx \
  --language-id <uuid-langue-yemba> \
  --base-url http://localhost:8080/api/content
```

Idempotent : relancer le script n'importe pas de doublons, les mots déjà
présents (même orthographe) sont ignorés.
