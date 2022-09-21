#!/bin/sh

# Previously, I have created a special class, which was responsible for setting up the data base.
# But, I discovered it was hard to ensure the setup process completes before the health check starts working.

CREATE_STATEMENTS="
create 'department', 'dp'
create 'department_name', 'dp'
create 'employee', 'em'
create 'employee_by_department_id', 'em'
"

echo "$CREATE_STATEMENTS" | hbase shell -n
