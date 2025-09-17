CREATE TABLE IF NOT EXISTS groupchat_constellation_creator.constellation_entries(
     id uuid NOT NULL ,
     constellation_id uuid NOT NULL,
     round_number integer NOT NULL,
     group_number integer NOT NULL,
     person_ids text NOT NULL,
     PRIMARY KEY (id)
);
