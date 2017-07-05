# --- !Ups

CREATE TABLE "commit"
(
  url     VARCHAR(255) NOT NULL,
  uuid    VARCHAR(255) NOT NULL,
  message TEXT         NOT NULL,
  UNIQUE ("url", "uuid")
);

# --- !Downs

DROP TABLE "commit";
