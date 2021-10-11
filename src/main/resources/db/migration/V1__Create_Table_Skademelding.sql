CREATE TABLE IF NOT EXISTS skademelding (
    id SERIAL PRIMARY KEY,
    json VARCHAR(250) NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    changed_time TIMESTAMP NOT NULL
    );