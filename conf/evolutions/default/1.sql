# --- !Ups

CREATE TABLE "foobar"
(
  foo     VARCHAR(255) NOT NULL UNIQUE,
  bar    VARCHAR(255) NOT NULL
);

# --- !Downs

DROP TABLE "foobar";
