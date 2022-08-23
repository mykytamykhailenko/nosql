CREATE KEYSPACE space WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': 3 };

CREATE TABLE space.employee (
  id uuid,
  department_id uuid,
  name text,
  surname text,
  PRIMARY KEY ((id), name, surname) -- I cannot update clustering keys.
);

CREATE TABLE space.department (
  id uuid,
  name text,
  description text,
  PRIMARY KEY ((id), name)
);

/*
I think I need a dedicated table for this operation, because:

1. I would need to read all employees from 'employee' by 'department_id', which is inefficient.
   Cassandra would need to query multiple partitions, which can be spread across the cluster.
   This is called Range Slice, and it happens when you don't select data by the partition key(s).
   https://www.instaclustr.com/support/documentation/cassandra/using-cassandra/avoiding-range-slice-issues-in-cassandra/
2. Secondly, I could partition 'employee' by ('department_id', 'id'), but I'll be unable to
   query 'employee' either by 'department_id' or 'id'. I'll have to provide both.
   https://stackoverflow.com/questions/27277025/querying-cassandra-by-a-partial-partition-key
*/
CREATE TABLE space.employee_by_department_id (
  department_id uuid,
  id uuid,
  name text,
  surname text,
  PRIMARY KEY ((department_id), name, surname, id)
) WITH CLUSTERING ORDER BY (name ASC, surname ASC);

