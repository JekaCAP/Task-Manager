--liquibase formatted sql

--changeset task-manager:001-schema
CREATE TABLE users (
    id              UUID         NOT NULL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    global_role     VARCHAR(20)  NOT NULL,
    avatar_url      VARCHAR(512),
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

CREATE TABLE refresh_tokens (
    id          UUID         NOT NULL PRIMARY KEY,
    user_id     UUID         NOT NULL,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE projects (
    id           UUID         NOT NULL PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    description  VARCHAR(2000),
    project_key  VARCHAR(10)  NOT NULL UNIQUE,
    archived     BOOLEAN      NOT NULL DEFAULT FALSE,
    owner_id     UUID         NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE project_members (
    id          UUID        NOT NULL PRIMARY KEY,
    project_id  UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    role        VARCHAR(20) NOT NULL,
    joined_at   TIMESTAMP   NOT NULL,
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_project_members UNIQUE (project_id, user_id)
);

CREATE TABLE tags (
    id          UUID         NOT NULL PRIMARY KEY,
    project_id  UUID         NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    color       VARCHAR(7),
    created_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_tags_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT uq_tags_project_name UNIQUE (project_id, name)
);

CREATE TABLE tasks (
    id           UUID         NOT NULL PRIMARY KEY,
    project_id   UUID         NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(4000),
    status       VARCHAR(20)  NOT NULL,
    priority     VARCHAR(20)  NOT NULL,
    assignee_id  UUID,
    due_date     TIMESTAMP,
    version      BIGINT       NOT NULL DEFAULT 0,
    deleted_at   TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users (id)
);

CREATE TABLE task_tags (
    task_id  UUID NOT NULL,
    tag_id   UUID NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE TABLE comments (
    id          UUID          NOT NULL PRIMARY KEY,
    task_id     UUID          NOT NULL,
    author_id   UUID          NOT NULL,
    body        VARCHAR(4000) NOT NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL,
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE attachments (
    id               UUID         NOT NULL PRIMARY KEY,
    task_id          UUID         NOT NULL,
    original_name    VARCHAR(255) NOT NULL,
    content_type     VARCHAR(100) NOT NULL,
    size_bytes       BIGINT       NOT NULL,
    storage_path     VARCHAR(512) NOT NULL,
    uploaded_by_id   UUID         NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    CONSTRAINT fk_attachments_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_attachments_user FOREIGN KEY (uploaded_by_id) REFERENCES users (id)
);

CREATE TABLE activity_logs (
    id          UUID         NOT NULL PRIMARY KEY,
    task_id     UUID         NOT NULL,
    actor_id    UUID         NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    details     VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_activity_logs_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_activity_logs_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_tasks_project_id ON tasks (project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_comments_task_id ON comments (task_id);
CREATE INDEX idx_activity_logs_task_id ON activity_logs (task_id);
