#!/bin/bash

LICENSE_HEADER=$(cat << 'EOF'
/*
 * Copyright (C) 2025 Zhiyu Zhou (jimzhouzzy@gmail.com)
 * This file is part of github.com/jimzhouzzy/Klotski.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
EOF
)

if [ ! -d "./core" ]; then
    echo "Error: ./core directory does not exist."
    exit 1
fi

find ./core -type f -name "*.java" | while read -r file; do
    if ! grep -q "GNU General Public License" "$file"; then
        temp_file=$(mktemp)
        echo "$LICENSE_HEADER" > "$temp_file"
        echo "" >> "$temp_file"
        cat "$file" >> "$temp_file"
        mv "$temp_file" "$file"
        echo "Added license to $file"
    else
        echo "License already exists in $file, skipping"
    fi
done

echo "License addition process completed."
