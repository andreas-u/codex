CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    gm_id UUID NOT NULL REFERENCES gm(id) ON DELETE CASCADE
);

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE object_grant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    object_id UUID NOT NULL,
    permission_id UUID NOT NULL REFERENCES permission(id),
    granted_by UUID REFERENCES "user"(id),
    grant_time TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX object_grant_user_idx ON object_grant(user_id);
CREATE INDEX object_grant_object_idx ON object_grant(object_id);
