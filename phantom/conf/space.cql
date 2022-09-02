CREATE KEYSPACE divisions WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': 3 };

CREATE TABLE divisions.employee (
  id UUID,
  department_id UUID,
  name TEXT,
  surname TEXT,
  PRIMARY KEY ((id), surname, name)
);

/*
-- 'employee_by_department_id' is a lookup table, which I must keep in sync with 'employee'.
-- This is a great use case for materialized views, but unfortunately it is an experimental feature.
CREATE MATERIALIZED VIEW divisions.employee_by_department_id AS
    SELECT * FROM divisions.employee
    WHERE department_id IS NOT NULL
    AND name IS NOT NULL
    AND surname IS NOT NULL
    AND id IS NOT NULL
    PRIMARY KEY ((department_id), surname, name, id);
*/

CREATE TABLE space.employee_by_department_id (
  department_id UUID,
  id UUID,
  name TEXT,
  surname TEXT,
  PRIMARY KEY ((department_id), name, surname, id)
) WITH CLUSTERING ORDER BY (name ASC, surname ASC);

CREATE TABLE divisions.department (
  id UUID,
  name TEXT,
  description TEXT,
  PRIMARY KEY ((id), name)
);

CREATE TABLE divisions.department_name (
  name TEXT PRIMARY KEY
);


