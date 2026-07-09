"""seed badge catalog

Revision ID: 6639e57a579a
Revises: dbe238ab3563
Create Date: 2026-07-09
"""
import uuid
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects.postgresql import UUID

# revision identifiers, used by Alembic.
revision: str = '6639e57a579a'
down_revision: Union[str, None] = 'dbe238ab3563'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

# Lightweight table reference for data-only migrations -- deliberately not
# importing app.db.entities.Badge here, so this migration stays valid even
# if the ORM model's shape changes later.
badges_table = sa.table(
    "badges",
    sa.column("id", UUID(as_uuid=True)),
    sa.column("code", sa.String),
    sa.column("name", sa.String),
    sa.column("description", sa.String),
    sa.column("criteria_type", sa.String),
    sa.column("criteria_value", sa.Integer),
)

BADGE_CODES = [
    "first_lesson",
    "streak_7",
    "streak_30",
    "words_100",
    "quiz_perfect",
]

BADGES = [
    {
        "code": "first_lesson",
        "name": "Premier pas",
        "description": "Terminer sa première leçon",
        "criteria_type": "lessons_completed",
        "criteria_value": 1,
    },
    {
        "code": "streak_7",
        "name": "Série de 7 jours",
        "description": "Maintenir une série d'activité de 7 jours consécutifs",
        "criteria_type": "streak",
        "criteria_value": 7,
    },
    {
        "code": "streak_30",
        "name": "Série de 30 jours",
        "description": "Maintenir une série d'activité de 30 jours consécutifs",
        "criteria_type": "streak",
        "criteria_value": 30,
    },
    {
        "code": "words_100",
        "name": "100 mots appris",
        "description": "Apprendre 100 mots au total, toutes langues confondues",
        "criteria_type": "words_learned",
        "criteria_value": 100,
    },
    {
        "code": "quiz_perfect",
        "name": "Perfectionniste",
        "description": "Terminer un quiz avec un score de 100%",
        "criteria_type": "quiz_perfect",
        "criteria_value": 1,
    },
]


def upgrade() -> None:
    op.bulk_insert(
        badges_table,
        [{"id": uuid.uuid4(), **badge} for badge in BADGES],
    )


def downgrade() -> None:
    conn = op.get_bind()
    conn.execute(
        badges_table.delete().where(badges_table.c.code.in_(BADGE_CODES))
    )
