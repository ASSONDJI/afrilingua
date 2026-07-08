"""
Word clustering by tonal/phonetic similarity, using K-Means.

Ported from services/recommendation-service source material:
~/yembalearn/04_kmeans_clustering.ipynb — restructured into a reusable,
testable service rather than notebook-style top-to-bottom script code.
"""
from dataclasses import dataclass

import numpy as np
import pandas as pd
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler

FEATURES = ["nb_syllabes", "longueur_mot", "ton1_num", "ton2_num"]


@dataclass
class ClusteringResult:
    cluster_labels: list[int]
    n_clusters: int
    silhouette_like_inertia: float


class ClusteringService:
    """Groups words into thematic/tonal clusters for lesson structuring."""

    def __init__(self, n_clusters: int = 8, random_state: int = 42):
        self.n_clusters = n_clusters
        self.random_state = random_state
        self._scaler = StandardScaler()
        self._model: KMeans | None = None

    def fit_predict(self, words_df: pd.DataFrame) -> ClusteringResult:
        """
        words_df must contain the columns listed in FEATURES.
        Missing values are not silently dropped: the caller is expected to
        have cleaned the data beforehand (fail loudly, not silently, on
        malformed input from content-service).
        """
        missing = [col for col in FEATURES if col not in words_df.columns]
        if missing:
            raise ValueError(f"Missing required columns for clustering: {missing}")

        X = words_df[FEATURES].to_numpy(dtype=float)
        X_scaled = self._scaler.fit_transform(X)

        self._model = KMeans(
            n_clusters=self.n_clusters,
            random_state=self.random_state,
            n_init=10,
        )
        labels = self._model.fit_predict(X_scaled)

        return ClusteringResult(
            cluster_labels=labels.tolist(),
            n_clusters=self.n_clusters,
            silhouette_like_inertia=float(self._model.inertia_),
        )
