import pandas as pd
import pytest

from app.services.pca_service import PcaService


def make_words_df() -> pd.DataFrame:
    return pd.DataFrame([
        {"nb_syllabes": 1, "longueur_mot": 3, "ton1_num": 0, "ton2_num": 0},
        {"nb_syllabes": 2, "longueur_mot": 4, "ton1_num": 0, "ton2_num": 1},
        {"nb_syllabes": 3, "longueur_mot": 6, "ton1_num": 1, "ton2_num": 1},
        {"nb_syllabes": 4, "longueur_mot": 8, "ton1_num": 2, "ton2_num": 2},
        {"nb_syllabes": 2, "longueur_mot": 5, "ton1_num": 1, "ton2_num": 0},
    ])


def test_fit_transform_returns_one_coordinate_pair_per_word():
    df = make_words_df()
    service = PcaService(n_components=2)

    result = service.fit_transform(df)

    assert len(result.coordinates) == len(df)
    assert all(len(coord) == 2 for coord in result.coordinates)


def test_fit_transform_returns_explained_variance_for_each_component():
    df = make_words_df()
    service = PcaService(n_components=2)

    result = service.fit_transform(df)

    assert len(result.explained_variance_ratio) == 2
    assert all(0 <= ratio <= 1 for ratio in result.explained_variance_ratio)


def test_fit_transform_raises_on_missing_columns():
    df = pd.DataFrame({"nb_syllabes": [1, 2]})

    service = PcaService()

    with pytest.raises(ValueError, match="Missing required columns"):
        service.fit_transform(df)
