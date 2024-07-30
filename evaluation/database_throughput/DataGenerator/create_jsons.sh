#!/bin/bash

# Directory containing the CSV files
CSV_DIR="../data"

# Directory to store the JSON files
JSON_DIR="json_data"

# Path to the Python script
PYTHON_SCRIPT="../csv_converter.py"

# Create the JSON directory if it does not exist
mkdir -p $JSON_DIR

# Loop through each CSV file in the directory
for CSV_FILE in $CSV_DIR/*.csv; do
    # Get the base name of the CSV file (without directory and extension)
    BASE_NAME=$(basename "$CSV_FILE" .csv)

    # Define the JSON file path
    JSON_FILE="$JSON_DIR/$BASE_NAME.json"

    # Convert CSV to JSON using the Python script and save to JSON file
    python3 $PYTHON_SCRIPT $CSV_FILE > $JSON_FILE

    echo "Converted $CSV_FILE to $JSON_FILE"
done
