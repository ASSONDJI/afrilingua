CREATE TABLE user_profiles (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               account_id UUID NOT NULL UNIQUE,
                               display_name VARCHAR(50) NOT NULL,
                               avatar_url VARCHAR(500),
                               bio VARCHAR(280),
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_profiles_account_id ON user_profiles(account_id);

CREATE TABLE user_preferences (
                                  profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
                                  language_code VARCHAR(10) NOT NULL,
                                  PRIMARY KEY (profile_id, language_code)
);

CREATE TABLE user_devices (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
                              device_id VARCHAR(255) NOT NULL,
                              device_type VARCHAR(20) NOT NULL,
                              last_active_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                              CONSTRAINT uq_user_devices_profile_device UNIQUE (profile_id, device_id)
);

CREATE TABLE activity_logs (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
                               activity_type VARCHAR(30) NOT NULL,
                               occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               metadata TEXT
);

CREATE INDEX idx_activity_logs_profile_occurred ON activity_logs(profile_id, occurred_at DESC);