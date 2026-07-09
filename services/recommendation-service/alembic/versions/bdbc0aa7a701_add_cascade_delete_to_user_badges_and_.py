"""add cascade delete to user_badges fk

Revision ID: bdbc0aa7a701
Revises: 3ea995fba524
Create Date: 2026-07-09
"""
from typing import Sequence, Union

from alembic import op

revision: str = "bdbc0aa7a701"
down_revision: Union[str, None] = "3ea995fba524"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # user_badges.progression_id n'avait pas ON DELETE CASCADE : un DELETE SQL brut
    # sur progressions echouait avec une violation de contrainte, alors que l'ORM
    # (relationship cascade="all, delete-orphan") s'en sortait car SQLAlchemy
    # supprime les enfants lui-meme avant le parent. On aligne la contrainte reelle
    # en base pour que l'integrite tienne meme hors du chemin ORM (scripts, autres
    # services, migrations de donnees).
    op.drop_constraint("user_badges_progression_id_fkey", "user_badges", type_="foreignkey")
    op.create_foreign_key(
        "user_badges_progression_id_fkey",
        "user_badges",
        "progressions",
        ["progression_id"],
        ["id"],
        ondelete="CASCADE",
    )


def downgrade() -> None:
    op.drop_constraint("user_badges_progression_id_fkey", "user_badges", type_="foreignkey")
    op.create_foreign_key(
        "user_badges_progression_id_fkey",
        "user_badges",
        "progressions",
        ["progression_id"],
        ["id"],
    )
