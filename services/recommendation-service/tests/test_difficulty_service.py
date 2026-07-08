import pandas as pd
import pytest

from app.services.difficulty_service import DifficultyService, apply_labeling_rule


def load_real_yemba_sample() -> pd.DataFrame:
    """
    Uses a realistic, varied sample (not artificially repeated rows) so
    the tree cannot shortcut through an accidental correlation the way
    a tiny synthetic test could. This mirrors the structure of the real
    YembaLearn dataset (dataset_maitre.csv): varied nb_syllabes and
    longueur_mot values within each tone group.
    """
    return pd.DataFrame([
        {"nb_syllabes": 2, "longueur_mot": 3, "ton1": "bas", "ton2": "bas"},
        {"nb_syllabes": 3, "longueur_mot": 6, "ton1": "bas", "ton2": "bas"},
        {"nb_syllabes": 2, "longueur_mot": 4, "ton1": "bas", "ton2": "bas"},
        {"nb_syllabes": 1, "longueur_mot": 2, "ton1": "bas", "ton2": "bas"},
        {"nb_syllabes": 4, "longueur_mot": 7, "ton1": "bas", "ton2": "bas"},
        {"nb_syllabes": 2, "longueur_mot": 5, "ton1": "bas", "ton2": "moyen"},
        {"nb_syllabes": 3, "longueur_mot": 4, "ton1": "moyen", "ton2": "bas"},
        {"nb_syllabes": 1, "longueur_mot": 3, "ton1": "bas", "ton2": "moyen"},
        {"nb_syllabes": 4, "longueur_mot": 8, "ton1": "moyen", "ton2": "bas"},
        {"nb_syllabes": 2, "longueur_mot": 6, "ton1": "bas", "ton2": "moyen"},
        {"nb_syllabes": 1, "longueur_mot": 3, "ton1": "haut", "ton2": "haut"},
        {"nb_syllabes": 3, "longueur_mot": 5, "ton1": "haut", "ton2": "moyen"},
        {"nb_syllabes": 2, "longueur_mot": 4, "ton1": "moyen", "ton2": "haut"},
        {"nb_syllabes": 4, "longueur_mot": 9, "ton1": "haut", "ton2": "bas"},
        {"nb_syllabes": 1, "longueur_mot": 2, "ton1": "haut", "ton2": "haut"},
    ])


def test_apply_labeling_rule_matches_documented_cases():
    assert apply_labeling_rule("bas", "bas") == "DEBUTANT"
    assert apply_labeling_rule("haut", "haut") == "AVANCE"
    assert apply_labeling_rule("bas", "haut") == "AVANCE"
    assert apply_labeling_rule("bas", "moyen") == "MOYEN"
    assert apply_labeling_rule("moyen", "bas") == "MOYEN"


def test_fit_reproduces_labeling_rule_near_perfectly():
    """
    This is the documented data-leakage behavior, not a bug: because the
    training labels are derived from ton1/ton2, and ton1_num/ton2_num are
    also used as input features, the tree is expected to reproduce the
    rule almost perfectly. This test exists to make that fact explicit
    and regression-tested, per the module's methodological note.
    """
    df = load_real_yemba_sample()
    service = DifficultyService(min_samples_leaf=2)  # small leaf size: sample is intentionally small

    result = service.fit(df)

    assert result.accuracy_on_training_rule >= 0.9
    assert "class:" in result.human_readable_rules


def test_classify_matches_labeling_rule_for_clear_cases():
    df = load_real_yemba_sample()
    service = DifficultyService(min_samples_leaf=2)
    service.fit(df)

    # Clear-cut cases where the labeling rule is unambiguous
    assert service.classify(nb_syllabes=2, longueur_mot=3, ton1="bas", ton2="bas") == "DEBUTANT"
    assert service.classify(nb_syllabes=1, longueur_mot=3, ton1="haut", ton2="haut") == "AVANCE"


def test_classify_before_fit_raises():
    service = DifficultyService()

    with pytest.raises(RuntimeError, match="fit\\(\\) must be called"):
        service.classify(nb_syllabes=2, longueur_mot=3, ton1="bas", ton2="bas")


def test_fit_raises_on_missing_columns():
    df = pd.DataFrame({"ton1": ["bas"], "ton2": ["bas"]})  # missing nb_syllabes, longueur_mot

    service = DifficultyService()

    with pytest.raises(ValueError, match="Missing required columns"):
        service.fit(df)
