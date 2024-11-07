#!/bin/bash

# Base command to run the Java program
JAVA_CMD="java -cp classes:lib/pddl4j-4.0.0.jar fr.uga.pddl4j.examples.mcts.MonteCarloPlanner"

# Root directory containing the test folders
TEST_DIR="src/test"

# List of main directories to process
DOMAINS=("blocks" "depots" "gripper" "logistics")

# Summary file to store results
SUMMARY_FILE="test_results_summary.txt"

# Initialize summary file
echo "Problem | Steps | Total Time (seconds)" > "$SUMMARY_FILE"

# Iterate over each domain
for domain in "${DOMAINS[@]}"; do
  # Find all domain.pddl files in subdirectories
  find "$TEST_DIR/$domain" -type f -name "domain.pddl" | while read -r domain_file; do
    # Get the directory of the domain file
    dir=$(dirname "$domain_file")
    
    # Find all problem files (e.g., p001.pddl, p002.pddl) in the same directory as the domain file
    find "$dir" -type f -name "p*.pddl" | while read -r problem_file; do
      # Output file to store each individual test result
      output_file="${problem_file%.pddl}_output.txt"
      
      # Run the Java command with the current domain and problem file, and save output
      echo "Running: $JAVA_CMD $domain_file $problem_file"
      $JAVA_CMD "$domain_file" "$problem_file" > "$output_file"
      
      # Extract total time and steps from output file
      total_time=$(grep "total time" "$output_file" | awk '{print $3}')
      steps=$(grep -c "([a-zA-Z-]+ [a-zA-Z0-9-]+)" "$output_file")

      # Append the result to the summary file
      echo "$(basename "$problem_file") | $steps | $total_time" >> "$SUMMARY_FILE"
    done
  done
done
