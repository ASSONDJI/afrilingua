"""add lessons_completed and has_perfect_quiz to progressions

Revision ID: 3ea995fba524
Revises: 3723ed00ff50
Create Date: 2026-07-09
"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op

revision: str = "3ea995fba524"
down_revision: Union[str, None] = "3723ed00ff50"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column(
        "progressions",
        sa.Column("lessons_completed", sa.Integer(), nullable=False, server_default="0"),
    )
    op.add_column(
        "progressions",
        sa.Column("has_perfect_quiz", sa.Boolean(), nullable=False, server_default=sa.false()),
    )


def downgrade() -> None:
    op.drop_column("progressions", "has_perfect_quiz")
    op.drop_column("progressions", "lessons_completed")
