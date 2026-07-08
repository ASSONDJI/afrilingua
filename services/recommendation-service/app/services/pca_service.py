"""
PCA-based dimensionality reduction for the learner statistics page:
projects each word's features into 2D so weak/strong points can be
visualized rather than read as a decorative chart.
"""
from dataclasses import dataclass

import numpy as np
import pandas as pd
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler

FEATURES = ["nb_syllabes", "longueur_mot", "ton1_num", "ton2_num"]


@dataclass
class PcaResult:
    coordinates: list[tuple[float, float]]
    explained_variance_ratio: list[float]


class PcaService:
    def __init__(self, n_components: int = 2):
        self.n_components = n_components
        self._scaler = StandardScaler()

    def fit_transform(self, words_df: pd.DataFrame) -> PcaResult:
        missing = [col for col in FEATURES if col not in words_df.columns]
        if missing:
            raise ValueError(f"Missing required columns for PCA: {missing}")

        X = words_df[FEATURES].to_numpy(dtype=float)
        X_scaled = self._scaler.fit_transform(X)

        pca = PCA(n_components=self.n_components, random_state=42)
        coords = pca.fit_transform(X_scaled)

        return PcaResult(
            coordinates=[tuple(row) for row in coords.tolist()],
            explained_variance_ratio=pca.explained_variance_ratio_.tolist(),
        )
