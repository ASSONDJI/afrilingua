from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_list_badges_returns_all_five_seeded_badges():
    response = client.get("/api/recommendations/badges")

    assert response.status_code == 200
    codes = {b["code"] for b in response.json()}
    assert codes == {
        "first_lesson",
        "quiz_perfect",
        "streak_7",
        "streak_30",
        "words_100",
    }


def test_badges_are_sorted_by_criteria_value():
    response = client.get("/api/recommendations/badges")

    values = [b["criteria_value"] for b in response.json()]
    assert values == sorted(values)
