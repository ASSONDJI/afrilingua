"""add learned_words table

Revision ID: 3723ed00ff50
Revises: 6639e57a579a
Create Date: 2026-07-09
"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects.postgresql import UUID

revision: str = "3723ed00ff50"
down_revision: Union[str, None] = "6639e57a579a"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "learned_words",
        sa.Column("id", UUID(as_uuid=True), primary_key=True),
        sa.Column(
            "progression_id",
            UUID(as_uuid=True),
            sa.ForeignKey("progressions.id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column("word_id", UUID(as_uuid=True), nullable=False),
        sa.Column("learned_at", sa.DateTime(timezone=True), nullable=False),
        sa.UniqueConstraint("progression_id", "word_id", name="uq_learned_words_progression_word"),
    )
    op.create_index("ix_learned_words_progression_id", "learned_words", ["progression_id"])


def downgrade() -> None:
    op.drop_index("ix_learned_words_progression_id", table_name="learned_words")
    op.drop_table("learned_words")
