-- Ensure documented demo accounts exist even when Flyway baselines an existing database.
INSERT INTO users (user_id, name, email, password, role)
VALUES (
        'DEMO_ADMIN',
        'Admin User',
        'admin@skyjet.com',
        '$2a$10$quJv4yincJtldL93qcc8Y.gaCIKlFANpQ.YB9kK24FgnPe7eMtRey',
        'ADMIN'
    )
ON CONFLICT (email) DO UPDATE
SET name = EXCLUDED.name,
    password = EXCLUDED.password,
    role = EXCLUDED.role;

INSERT INTO users (user_id, name, email, password, role)
VALUES (
        'DEMO_USER',
        'James Smith',
        'james@skyjet.com',
        '$2a$10$19moy27Wjs0YyEZ6Dm3lNeBFyWjGTouSA19Tq6a9yalbsKHVeJW46',
        'USER'
    )
ON CONFLICT (email) DO UPDATE
SET name = EXCLUDED.name,
    password = EXCLUDED.password,
    role = EXCLUDED.role;
