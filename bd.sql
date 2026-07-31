CREATE TABLE User (
    uuid TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE World (
    world_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    is_blocked INTEGER NOT NULL
);

CREATE TABLE Home (
    owner_uuid TEXT PRIMARY KEY,
    world_id INTEGER NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    yaw REAL NOT NULL,
    pitch REAL NOT NULL,
    FOREIGN KEY (owner_uuid) REFERENCES User(uuid),
    FOREIGN KEY (world_id) REFERENCES World(world_id)
);

CREATE TABLE Warp (
    warp_id INTEGER PRIMARY KEY,
    owner_uuid TEXT NOT NULL,
    world_id INTEGER NOT NULL,
    warp_name TEXT NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    yaw REAL NOT NULL,
    pitch REAL NOT NULL,
    FOREIGN KEY (owner_uuid) REFERENCES User(uuid),
    FOREIGN KEY (world_id) REFERENCES World(world_id),
    UNIQUE (owner_uuid, warp_name)
);

CREATE TABLE Inventory (
    inventory_id TEXT PRIMARY KEY,
    data BLOB NOT NULL
);

CREATE TABLE Teleport (
    request_id INTEGER PRIMARY KEY,
    sender_uuid TEXT NOT NULL,
    receiver_uuid TEXT NOT NULL,
    is_tpa INTEGER NOT NULL DEFAULT 0, -- 0 TPA, 1 TPAHERE
    timeout INTEGER NOT NULL DEFAULT 60, -- seconds
    FOREIGN KEY (sender_uuid) REFERENCES User(uuid),
    FOREIGN KEY (receiver_uuid) REFERENCES User(uuid),
    UNIQUE (sender_uuid, receiver_uuid)
);