import pandas as pd
import pytest

from app.services.clustering_service import ClusteringService


def make_words_df(n_per_group: int = 5) -> pd.DataFrame:
    """Three visually distinct groups in feature space, so K-Means has
    an unambiguous structure to find — not testing tone semantics here,
    just that the clustering pipeline works end to end."""
    rows = []
    for i in range(n_per_group):
        rows.append({"nb_syllabes": 1, "longueur_mot": 3, "ton1_num": 0, "ton2_num": 0})
    for i in range(n_per_group):
        rows.append({"nb_syllabes": 3, "longueur_mot": 8, "ton1_num": 1, "ton2_num": 1})
    for i in range(n_per_group):
        rows.append({"nb_syllabes": 5, "longueur_mot": 12, "ton1_num": 2, "ton2_num": 2})
    return pd.DataFrame(rows)


def test_fit_predict_returns_one_label_per_word():
    df = make_words_df()
    service = ClusteringService(n_clusters=3)

    result = service.fit_predict(df)

    assert len(result.cluster_labels) == len(df)
    assert result.n_clusters == 3


def test_fit_predict_groups_distinct_clusters_separately():
    """Words from the same visually-distinct group should end up in the
    same cluster more often than with words from a different group."""
    df = make_words_df(n_per_group=5)
    service = ClusteringService(n_clusters=3)

    result = service.fit_predict(df)
    labels = result.cluster_labels

    first_group_labels = labels[0:5]
    second_group_labels = labels[5:10]

    # Within a clearly separated group, K-Means should assign a single
    # dominant cluster to most members.
    assert len(set(first_group_labels)) == 1
    assert len(set(second_group_labels)) == 1
    assert first_group_labels[0] != second_group_labels[0]


def test_fit_predict_raises_on_missing_columns():
    df = pd.DataFrame({"nb_syllabes": [1, 2]})  # missing required columns

    service = ClusteringService(n_clusters=2)

    with pytest.raises(ValueError, match="Missing required columns"):
        service.fit_predict(df)
