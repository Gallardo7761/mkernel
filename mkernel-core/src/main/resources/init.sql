CREATE TABLE IF NOT EXISTS User (
    uuid TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS World (
    world_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    is_blocked INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS Home (
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

CREATE TABLE IF NOT EXISTS Warp (
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

CREATE TABLE IF NOT EXISTS Inventory (
    inventory_id TEXT PRIMARY KEY,
    data BLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS Teleport (
    request_id INTEGER PRIMARY KEY,
    sender_uuid TEXT NOT NULL,
    receiver_uuid TEXT NOT NULL,
    is_tpa INTEGER NOT NULL DEFAULT 0,
    timeout INTEGER NOT NULL DEFAULT 60,
    FOREIGN KEY (sender_uuid) REFERENCES User(uuid),
    FOREIGN KEY (receiver_uuid) REFERENCES User(uuid),
    UNIQUE (sender_uuid, receiver_uuid)
);

CREATE TABLE IF NOT EXISTS Shop (
    shop_id TEXT PRIMARY KEY,
    owner_uuid TEXT NOT NULL,
    world_id INTEGER NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    item_data BLOB NOT NULL,
    price REAL NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (owner_uuid) REFERENCES User(uuid),
    FOREIGN KEY (world_id) REFERENCES World(world_id),
    UNIQUE (world_id, x, y, z)
);