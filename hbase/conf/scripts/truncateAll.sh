#!/bin/sh

TRUNCATE_STATEMENTS="
truncate 'department'
truncate 'department_name'
truncate 'employee'
truncate 'employee_by_department_id'
"

echo "$TRUNCATE_STATEMENTS" | hbase shell -n
