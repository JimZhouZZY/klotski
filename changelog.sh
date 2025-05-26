#!/bin/bash

# Process all .java files in current directory and subdirectories
for java_file in $(find . -name "*.java"); do
    echo "Processing: $java_file"

    # Create temporary file for processing
    temp_file=$(mktemp)

    # Get all git commits for this file (only message and date)
    git_log=$(git log refs/heads/main --follow --pretty=format:"%ad: %s" --date=short -- "$java_file")

    # Initialize variables
    version_line=""
    changelog_found=false
    changelog_entries=0

    # First pass: parse existing file content to preserve non-changelog parts
    while IFS= read -r line; do
        # Capture version line if it exists
        if [[ "$line" == *"@version"* ]]; then
            version_line="$line"
        fi

        # Detect changelog section start
        if [[ "$line" == *"Change log:"* ]]; then
            changelog_found=true
            break
        fi
    done < "$java_file"

    # Count the number of git log entries
    changelog_entries=$(echo "$git_log" | grep -c "[0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}")

    # Calculate new version (1.x where x = number of entries)
    new_version="1.$((changelog_entries))"

    # Second pass: rewrite the entire file with new changelog
    in_changelog=false
    changelog_written=false

    while IFS= read -r line; do
        # Write version line (update if exists, add if not)
        if [[ "$line" == *"@version"* ]]; then
            echo " * @version $new_version" >> "$temp_file"
            continue
        fi

        # Handle changelog section start
        if [[ "$line" == *"Change log:"* ]]; then
            in_changelog=true
            changelog_written=true
            echo " * Change log:" >> "$temp_file"

            # Add all git log entries
            while IFS= read -r log_entry; do
                # Skip empty lines
                if [[ -z "$log_entry" ]]; then
                    continue
                fi
                echo " * $log_entry" >> "$temp_file"
            done <<< "$git_log"

            continue
        fi

        # Skip old changelog content
        if [[ "$in_changelog" == true ]]; then
            # Detect end of changelog section
            if [[ "$line" == *"*/"* ]]; then
                in_changelog=false
                echo "$line" >> "$temp_file"
            fi
            continue
        fi

        # Write all other lines
        echo "$line" >> "$temp_file"
    done < "$java_file"

    # If we didn't find a place to insert the changelog (malformed file), add it before the */ line
    if [[ "$changelog_written" == false ]]; then
        # Read the entire file and process it differently
        file_content=$(cat "$java_file")
        # Find the last comment line before */
        new_content=$(echo "$file_content" | sed "/\*\//i\ * Change log:")
        # Insert all log entries
        while IFS= read -r log_entry; do
            # Skip empty lines
            if [[ -z "$log_entry" ]]; then
                continue
            fi
            new_content=$(echo "$new_content" | sed "/Change log:/a\ * $log_entry")
        done <<< "$git_log"
        # Update version
        if [[ -n "$version_line" ]]; then
            new_content=$(echo "$new_content" | sed "s/@version .*/@version $new_version/")
        else
            # Find the last @ line and add version after it
            new_content=$(echo "$new_content" | sed "/@/a\ * @version $new_version")
        fi
        echo "$new_content" > "$temp_file"
    fi

    # Always replace the original file since we're regenerating the changelog completely
    mv "$temp_file" "$java_file"
    echo "Updated $java_file to version $new_version with $changelog_entries entries"
done
