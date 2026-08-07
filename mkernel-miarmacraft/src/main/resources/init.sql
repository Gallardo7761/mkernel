CREATE TABLE IF NOT EXISTS CrimeHistory (
    crime_id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL,
    crime TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    count INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (uuid) REFERENCES User(uuid),
    UNIQUE (uuid, crime)
);