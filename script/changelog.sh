#!/bin/bash

# Process all .java files in current directory and subdirectories
for java_file in $(find ../ -name "*.java"); do
    echo "Processing: $java_file"

    # Create temporary file for processing
    temp_file=$(mktemp)

    # Get all git commits for this file (only message and date)
    git_log=$(git log --follow --pretty=format:"%ad: %s" --date=short -- "$java_file")

    # Initialize variables
    changelog_entries=0
    in_changelog=false
    changelog_start=0
    changelog_content=""
    version_line=""
    new_entries_added=0

    # First pass: parse existing file content
    while IFS= read -r line; do
        # Capture version line
        if [[ "$line" == *"@version"* ]]; then
            version_line="$line"
        fi

        # Detect changelog section start
        if [[ "$line" == *"Change log:"* ]]; then
            in_changelog=true
            changelog_start=1
            changelog_content="$line"$'\n'
            continue
        fi

        # Process changelog content
        if [[ "$in_changelog" == true ]]; then
            # Count existing entries (lines starting with " * YYYY-MM-DD")
            if [[ "$line" =~ ^\ \*\ [0-9]{4}-[0-9]{2}-[0-9]{2} ]]; then
                ((changelog_entries++))
            fi

            # Detect changelog section end
            if [[ "$line" == *"*/"* ]]; then
                in_changelog=false
            else
                changelog_content+="$line"$'\n'
            fi
        fi
    done < "$java_file"

    # Calculate new version (1.x where x = number of entries - 1)
    new_version="1.$((changelog_entries))"

    # Check if there are new git commits not in changelog
    new_commits=()
    while IFS= read -r log_entry; do
        # Skip empty lines
        if [[ -z "$log_entry" ]]; then
            continue
        fi

        # Check if this commit is already in changelog
        if ! grep -q "$log_entry" "$java_file"; then
            new_commits+=("$log_entry")
        fi
    done <<< "$git_log"

    # Only proceed if there are new commits or no changelog exists
    if [[ ${#new_commits[@]} -gt 0 || $changelog_entries -eq 0 ]]; then
        # Second pass: write new file content
        in_changelog=false
        changelog_written=false

        while IFS= read -r line; do
            # Write version line
            if [[ "$line" == *"@version"* ]]; then
                echo " * @version $new_version" >> "$temp_file"
                continue
            fi

            # Handle changelog section
            if [[ "$line" == *"Change log:"* ]]; then
                in_changelog=true
                changelog_written=true
                echo "$line" >> "$temp_file"

                # Add new commits
                for entry in "${new_commits[@]}"; do
                    echo " * $entry" >> "$temp_file"
                    ((changelog_entries++))
                done

                # Skip writing the old content (we'll add it back later)
                continue
            fi

            if [[ "$in_changelog" == true && "$line" == *"*/"* ]]; then
                in_changelog=false
            fi

            # Skip old changelog content during processing
            if [[ "$in_changelog" == true ]]; then
                continue
            fi

            # Write all other lines
            echo "$line" >> "$temp_file"
        done < "$java_file"

        # If changelog wasn't written (didn't exist), add it
        if [[ "$changelog_written" == false ]]; then
            # Find last line before */ (end of comment)
            sed -i "/\*\//i\ * Change log:" "$temp_file"
            for entry in "${new_commits[@]}"; do
                sed -i "/Change log:/a\ * $entry" "$temp_file"
                ((changelog_entries++))
            done
            # Update version again in case we added entries
            new_version="1.$((changelog_entries))"
            sed -i "s/@version .*/@version $new_version/" "$temp_file"
        fi

        # Replace original file if we made changes
        if [[ ${#new_commits[@]} -gt 0 ]]; then
            mv "$temp_file" "$java_file"
            echo "Updated $java_file to version $new_version with ${#new_commits[@]} new entries"
        else
            rm "$temp_file"
            echo "No changes needed for $java_file (version $new_version)"
        fi
    else
        rm "$temp_file"
        echo "No new commits for $java_file (version $new_version remains unchanged)"
    fi
done

echo "All .java files processed"
