import pytest
from fastapi.testclient import TestClient

from app.db.session import get_db
from app.main import app
from app.services.gamification_service import handle_lesson_completed

client = TestClient(app)


@pytest.fixture
def client_using_test_db(db_session):
    """Redirige Depends(get_db) vers la meme session de test transactionnelle
    que celle utilisee pour ecrire les donnees du test, pour que l'appel HTTP
    du TestClient voie ce qui vient d'etre ecrit dans la meme transaction."""
    def _override():
        yield db_session

    app.dependency_overrides[get_db] = _override
    yield client
    app.dependency_overrides.pop(get_db, None)


def test_unknown_user_returns_zeroed_progression_not_404():
    """
    Deliberately NOT a 404: 'no activity yet' is a normal state for a new
    user, not an error condition the mobile app should have to branch on.
    """
    response = client.get(
        "/api/recommendations/progressions/99999999-9999-9999-9999-999999999999"
    )

    assert response.status_code == 200
    body = response.json()
    assert body["xp"] == 0
    assert body["level"] == 1
    assert body["current_streak"] == 0
    assert body["badges"] == []


def test_existing_user_returns_real_progression_and_badges(
    client_using_test_db, db_session, make_user_id
):
    user_id = make_user_id()
    handle_lesson_completed(db_session, {"user_id": str(user_id)})

    response = client_using_test_db.get(f"/api/recommendations/progressions/{user_id}")

    assert response.status_code == 200
    body = response.json()
    assert body["xp"] == 50
    assert body["lessons_completed"] == 1
    assert any(badge["code"] == "first_lesson" for badge in body["badges"])


def test_malformed_uuid_returns_422():
    response = client.get("/api/recommendations/progressions/not-a-valid-uuid")

    assert response.status_code == 422
