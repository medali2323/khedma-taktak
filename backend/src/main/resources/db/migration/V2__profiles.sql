CREATE TABLE profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    slug VARCHAR(30) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    theme VARCHAR(50) NOT NULL DEFAULT 'classic',
    photo_url VARCHAR(500),
    full_name JSON NOT NULL,
    title JSON,
    summary JSON,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    contact_location JSON,
    social_links JSON,
    primary_trade VARCHAR(100),
    trade_specialties JSON,
    driving_license VARCHAR(20),
    has_own_vehicle BOOLEAN NOT NULL DEFAULT FALSE,
    mobility_radius_km INT,
    tools_equipment JSON,
    student_institution VARCHAR(255),
    student_year VARCHAR(50),
    internship_sought VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_profiles_slug UNIQUE (slug),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE experiences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    company JSON NOT NULL,
    role JSON NOT NULL,
    location JSON,
    start_date DATE NOT NULL,
    end_date DATE,
    current_position BOOLEAN NOT NULL DEFAULT FALSE,
    description JSON,
    highlights JSON,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_experiences_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    INDEX idx_experiences_profile_id (profile_id)
);

CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    title JSON NOT NULL,
    description JSON,
    url VARCHAR(500),
    github_url VARCHAR(500),
    image_urls JSON,
    technologies JSON,
    highlights JSON,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    INDEX idx_projects_profile_id (profile_id)
);

CREATE TABLE educations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    institution JSON NOT NULL,
    degree JSON NOT NULL,
    field JSON,
    start_date DATE,
    end_date DATE,
    description JSON,
    education_type VARCHAR(20) NOT NULL DEFAULT 'ACADEMIC',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_educations_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    INDEX idx_educations_profile_id (profile_id)
);

CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    name JSON NOT NULL,
    category JSON,
    level INT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_skills_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    INDEX idx_skills_profile_id (profile_id)
);

CREATE TABLE language_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    language JSON NOT NULL,
    level JSON NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_language_skills_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    INDEX idx_language_skills_profile_id (profile_id)
);

CREATE TABLE certifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    name JSON NOT NULL,
    issuer JSON,
    issue_date DATE,
    expiry_date DATE,
    credential_url VARCHAR(500),
    certification_type VARCHAR(20) NOT NULL DEFAULT 'PROFESSIONAL',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_certifications_profile FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,
    INDEX idx_certifications_profile_id (profile_id)
);
