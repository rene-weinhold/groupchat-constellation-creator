CREATE TABLE IF NOT EXISTS groupchat_constellation_creator.people(
     id uuid NOT NULL,
     constellation_id uuid NOT NULL,
     name varchar(256) NOT NULL,
     PRIMARY KEY (id)
);
