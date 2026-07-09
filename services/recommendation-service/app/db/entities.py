import uuid
from datetime import date, datetime

from sqlalchemy import Column, String, Integer, Date, DateTime, ForeignKey, UniqueConstraint, Boolean
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.db.session import Base


class Progression(Base):
    """
    One row per user. Holds derived/computed gamification state (XP, level,
    streak) -- distinct from user-service's activity_logs, which holds the
    raw activity events this state is computed from (cahier des charges §7).
    """
    __tablename__ = "progressions"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), nullable=False, unique=True, index=True)
    xp = Column(Integer, nullable=False, default=0)
    level = Column(Integer, nullable=False, default=1)
    current_streak = Column(Integer, nullable=False, default=0)
    longest_streak = Column(Integer, nullable=False, default=0)
    last_activity_date = Column(Date, nullable=True)
    updated_at = Column(DateTime, nullable=False, default=datetime.utcnow, onupdate=datetime.utcnow)
    lessons_completed = Column(Integer, nullable=False, default=0)
    has_perfect_quiz = Column(Boolean, nullable=False, default=False)

    user_badges = relationship("UserBadge", back_populates="progression", cascade="all, delete-orphan")
    learned_words = relationship("LearnedWord", back_populates="progression", cascade="all, delete-orphan")


class Badge(Base):
    """
    Fixed catalog of unlockable badges. Seeded via Alembic data migration,
    not user-editable at runtime.
    """
    __tablename__ = "badges"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    code = Column(String(50), nullable=False, unique=True)
    name = Column(String(100), nullable=False)
    description = Column(String(255), nullable=False)
    criteria_type = Column(String(50), nullable=False)  # e.g. "streak", "lessons_completed", "quiz_perfect"
    criteria_value = Column(Integer, nullable=False)


class UserBadge(Base):
    """Join table: which badges a user has earned, and when."""
    __tablename__ = "user_badges"
    __table_args__ = (UniqueConstraint("progression_id", "badge_id", name="uq_user_badge"),)

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    progression_id = Column(UUID(as_uuid=True), ForeignKey("progressions.id"), nullable=False)
    badge_id = Column(UUID(as_uuid=True), ForeignKey("badges.id"), nullable=False)
    earned_at = Column(DateTime, nullable=False, default=datetime.utcnow)

    progression = relationship("Progression", back_populates="user_badges")
    badge = relationship("Badge")


class LearnedWord(Base):
    __tablename__ = "learned_words"
    __table_args__ = (
        UniqueConstraint("progression_id", "word_id", name="uq_learned_words_progression_word"),
    )

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    progression_id = Column(
        UUID(as_uuid=True), ForeignKey("progressions.id", ondelete="CASCADE"), nullable=False, index=True
    )
    word_id = Column(UUID(as_uuid=True), nullable=False)
    learned_at = Column(DateTime, nullable=False, default=datetime.utcnow)

    progression = relationship("Progression", back_populates="learned_words")
