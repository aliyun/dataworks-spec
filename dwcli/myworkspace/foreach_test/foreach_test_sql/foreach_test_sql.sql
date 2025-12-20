-- SQL script for foreach_test_sql
-- This SQL runs for each item in the loop

SELECT '${item}' as loop_item, 
       '${loop_index}' as loop_index,
       CURRENT_TIMESTAMP as exec_time;