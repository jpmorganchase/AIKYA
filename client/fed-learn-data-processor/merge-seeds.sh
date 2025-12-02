#!/bin/sh

# Define the directories
EXTERNAL_SEEDS_DIR="/apps/data/external"
TARGET_SEEDS_DIR="/apps/data/seeds"

echo "Checking for external seed files to merge..."

# Check if the external directory exists and has files to merge
if [ -d "$EXTERNAL_SEEDS_DIR" ] && [ "$(ls -A $EXTERNAL_SEEDS_DIR)" ]; then
  echo "Merging external seed files into $TARGET_SEEDS_DIR..."

  # Copy files from the external seeds directory to the main seeds directory
  cp -r "$EXTERNAL_SEEDS_DIR"/* "$TARGET_SEEDS_DIR/"

  echo "Merge completed."
else
  echo "No external seed files found to merge."
fi
