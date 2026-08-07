CREATE TABLE IF NOT EXISTS Tithe (
    uuid TEXT PRIMARY KEY,
    due_date INTEGER NOT NULL,
    amount REAL NOT NULL,
    FOREIGN KEY (uuid) REFERENCES User(uuid)
);

CREATE TABLE IF NOT EXISTS Fine (
    fine_id TEXT PRIMARY KEY,
    target_uuid TEXT NOT NULL,
    issuer_uuid TEXT NOT NULL,
    amount REAL NOT NULL,
    reason TEXT NOT NULL,
    issue_date INTEGER NOT NULL,
    due_date INTEGER NOT NULL,
    is_paid INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (target_uuid) REFERENCES User(uuid)
);

CREATE TABLE IF NOT EXISTS CrimeHistory (
    crime_id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL,
    crime TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    FOREIGN KEY (uuid) REFERENCES User(uuid)
);