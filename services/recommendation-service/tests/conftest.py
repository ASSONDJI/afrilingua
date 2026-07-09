import uuid

import pytest
from sqlalchemy import create_engine, event
from sqlalchemy.orm import sessionmaker

TEST_DATABASE_URL = (
    "postgresql+psycopg2://afrilingua:afrilingua_dev_password@localhost:5438/afrilingua_recommendation_test"
)


@pytest.fixture(scope="session")
def db_engine():
    engine = create_engine(TEST_DATABASE_URL)
    yield engine
    engine.dispose()


@pytest.fixture
def db_session(db_engine):
    """
    Session de test isolee. gamification_service.py appelle session.commit()
    plusieurs fois par test (handle_quiz_completed, handle_lesson_completed) :
    un simple `connection.begin()` + `session.rollback()` en fin de test ne
    suffirait pas, car un commit() interne cloturerait la transaction externe
    avant l'heure. On utilise donc le pattern SQLAlchemy "nested savepoint" :
    - une transaction externe encadre tout le test
    - un SAVEPOINT absorbe les commit() internes du service
    - un event listener recree un SAVEPOINT a chaque fois qu'un commit() le
      cloture, pour que le suivant soit toujours pret
    - le rollback() final de la transaction externe efface tout, meme les
      commits internes du service
    """
    connection = db_engine.connect()
    transaction = connection.begin()

    Session = sessionmaker(bind=connection)
    session = Session()

    nested = connection.begin_nested()

    @event.listens_for(session, "after_transaction_end")
    def restart_savepoint(sess, trans):
        nonlocal nested
        if not nested.is_active:
            nested = connection.begin_nested()

    yield session

    session.close()
    transaction.rollback()
    connection.close()


@pytest.fixture
def make_user_id():
    """Genere un UUID different a chaque appel, pour eviter les collisions
    entre tests meme si l'isolation par rollback echouait pour une raison
    quelconque."""
    def _make():
        return uuid.uuid4()
    return _make
