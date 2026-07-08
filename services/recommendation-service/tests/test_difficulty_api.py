from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_apply_rule_returns_debutant_for_bas_bas():
    response = client.post(
        "/api/recommendations/difficulty/apply-rule",
        json={"ton1": "bas", "ton2": "bas"},
    )

    assert response.status_code == 200
    assert response.json() == {"niveau": "DEBUTANT"}


def test_apply_rule_returns_avance_for_haut_haut():
    response = client.post(
        "/api/recommendations/difficulty/apply-rule",
        json={"ton1": "haut", "ton2": "haut"},
    )

    assert response.status_code == 200
    assert response.json() == {"niveau": "AVANCE"}


def test_apply_rule_returns_moyen_for_mixed_tones():
    response = client.post(
        "/api/recommendations/difficulty/apply-rule",
        json={"ton1": "bas", "ton2": "moyen"},
    )

    assert response.status_code == 200
    assert response.json() == {"niveau": "MOYEN"}


def test_apply_rule_does_not_depend_on_train_having_been_called():
    """
    Regression test for the exact bug found during manual integration
    testing: /classify requires /train to have been called first in the
    same process, causing every content-service call to silently fall
    back to BEGINNER. /apply-rule must work immediately, with no prior
    state, since it calls apply_labeling_rule directly rather than a
    trained model.
    """
    response = client.post(
        "/api/recommendations/difficulty/apply-rule",
        json={"ton1": "haut", "ton2": "bas"},
    )

    assert response.status_code == 200
    assert response.json()["niveau"] == "AVANCE"
