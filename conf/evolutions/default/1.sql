# --- !Ups

CREATE TABLE "projects"
(
  -- I don't feel confortable in having an url as a Primary Key.
  id     VARCHAR(255) NOT NULL,
  CONSTRAINT pk_projects PRIMARY KEY (id)
);

-- This is not supported by SQLite
-- ALTER TABLE "projects" ADD CONSTRAINT PK_PROJECTS PRIMARY KEY (id);

CREATE TABLE "commits"
(
  project_id VARCHAR(255) NOT NULL,
  commit_id VARCHAR(255) NOT NULL,
  -- I'm not sure if commit_id should be UNIQUE
  commit_message VARCHAR(2048) NOT NULL,
  timestamp INTEGER NOT NULL,
  CONSTRAINT pk_commits PRIMARY KEY (project_id, commit_id),
  CONSTRAINT fk_projects FOREIGN KEY (project_id) REFERENCES "projects"(id)
);

--ALTER TABLE "commits" ADD CONSTRAINT PRIMARY KEY (project_id, commit_id);

# --- !Downs

-- Personally, i think this is hazardous!
--DROP TABLE "foobar";
--DROP TABLE "commits";
--DROP TABLE "projects";