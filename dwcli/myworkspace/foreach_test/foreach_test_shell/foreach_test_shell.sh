#!/bin/bash
# Shell script for foreach_test_shell
# This shell script runs for each item in the loop

echo "=========================================="
echo "Processing item: ${item}"
echo "Loop index: ${loop_index}"
echo "Current time: $(date)"
echo "=========================================="

# TODO: Add your loop processing logic here
echo "Hello from foreach iteration ${loop_index}"