"""
Difficulty-level rule extraction for Yemba-family words, using a
decision tree (C4.5-equivalent via scikit-learn's entropy criterion).

IMPORTANT — methodological note (see project report, section on
recommendation-service):
The training labels are themselves derived from a deterministic rule
on tone1/tone2 (see LABELING_RULE below), and that same tone data is
also used as input features. This means the tree does not "predict"
difficulty from independent signal — it recovers, via induction, the
labeling rule that produced its own training data. This is a form of
data leakage if framed as a predictive model.

What this module actually is: a rule-extraction / knowledge-discovery
tool. Its value is that it turns a hand-written if/elif/else rule
(originally defined by the YembaLearn project, see LABELING_RULE)
into an interpretable, re-trainable model that:
  - can be re-derived automatically for each new language added to the
    catalogue, without a linguist re-writing the rule by hand each time
  - produces a human-readable rule set (export_text) that a linguist
    can review and validate, rather than trusting a black box

It is explicitly NOT used to predict how hard a word will be for a
given learner — that requires real usage data (see PHASE 2 note in
the project roadmap: a future model trained on activity_logs, once
enough real attempt data exists).
"""
from dataclasses import dataclass

import numpy as np
import pandas as pd
from sklearn.tree import DecisionTreeClassifier, export_text
from sklearn.preprocessing import LabelEncoder

FEATURES = ["nb_syllabes", "longueur_mot", "ton1_num", "ton2_num"]
TONE_ORDER = {"bas": 0, "moyen": 1, "haut": 2}


def apply_labeling_rule(tone1: str, tone2: str) -> str:
    """
    The original hand-written rule from YembaLearn (07_arbres_decision.ipynb),
    kept here as the single source of truth for what "ground truth" means
    in this module. The tree in DifficultyService is trained to reproduce
    this exact function via induction from (tone1, tone2)-derived features.
    """
    if tone1 == "bas" and tone2 == "bas":
        return "DEBUTANT"
    elif tone1 == "haut" or tone2 == "haut":
        return "AVANCE"
    else:
        return "MOYEN"


@dataclass
class DifficultyRuleResult:
    accuracy_on_training_rule: float
    human_readable_rules: str
    depth: int
    n_leaves: int


class DifficultyService:
    """Extracts an interpretable difficulty-classification rule via C4.5-style induction."""

    def __init__(self, max_depth: int = 4, min_samples_leaf: int = 5, random_state: int = 42):
        self._model = DecisionTreeClassifier(
            criterion="entropy",
            max_depth=max_depth,
            min_samples_leaf=min_samples_leaf,
            random_state=random_state,
        )
        self._label_encoder = LabelEncoder()
        self._fitted = False

    def fit(self, words_df: pd.DataFrame) -> DifficultyRuleResult:
        """
        words_df must contain: nb_syllabes, longueur_mot, ton1, ton2.
        The DEBUTANT/MOYEN/AVANCE label is derived here via apply_labeling_rule,
        not read from an external "ground truth" column — because there isn't
        one; see module docstring.
        """
        required = ["nb_syllabes", "longueur_mot", "ton1", "ton2"]
        missing = [col for col in required if col not in words_df.columns]
        if missing:
            raise ValueError(f"Missing required columns for difficulty rule extraction: {missing}")

        df = words_df.copy()
        df["ton1_num"] = df["ton1"].map(TONE_ORDER).fillna(0)
        df["ton2_num"] = df["ton2"].map(TONE_ORDER).fillna(0)
        df["niveau"] = df.apply(lambda row: apply_labeling_rule(row["ton1"], row["ton2"]), axis=1)

        X = df[FEATURES].to_numpy(dtype=float)
        y = self._label_encoder.fit_transform(df["niveau"])

        self._model.fit(X, y)
        self._fitted = True

        rules = export_text(
            self._model,
            feature_names=FEATURES,
            class_names=list(self._label_encoder.classes_),
        )

        return DifficultyRuleResult(
            accuracy_on_training_rule=float(self._model.score(X, y)),
            human_readable_rules=rules,
            depth=self._model.get_depth(),
            n_leaves=self._model.get_n_leaves(),
        )

    def classify(self, nb_syllabes: int, longueur_mot: int, ton1: str, ton2: str) -> str:
        if not self._fitted:
            raise RuntimeError("DifficultyService.fit() must be called before classify()")

        ton1_num = TONE_ORDER.get(ton1, 0)
        ton2_num = TONE_ORDER.get(ton2, 0)
        X = np.array([[nb_syllabes, longueur_mot, ton1_num, ton2_num]])
        prediction = self._model.predict(X)
        return self._label_encoder.inverse_transform(prediction)[0]
