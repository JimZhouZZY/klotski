#!/bin/bash

# publish_docs.sh - Generates and publishes core component Javadoc to GitHub Pages

# Exit on error and print each command
set -ex

# Configuration
REPO_URL=$(git remote get-url origin)
REPO_NAME=$(basename -s .git "$REPO_URL")
DOCS_DIR="core/build/docs/javadoc"
TARGET_BRANCH="gh-pages"
TEMP_DIR=$(mktemp -d)

# Generate Javadoc using Gradle
echo "Generating Javadoc for core component..."
./gradlew :core:javadoc

# Check if javadoc was generated
if [ ! -d "$DOCS_DIR" ]; then
  echo "Error: Javadoc not generated at $DOCS_DIR"
  exit 1
fi

# Clone the repository into a temporary directory
echo "Preparing to publish documentation..."
git clone --branch "$TARGET_BRANCH" --single-branch "$REPO_URL" "$TEMP_DIR" || {
  # If gh-pages branch doesn't exist, create it
  git clone "$REPO_URL" "$TEMP_DIR"
  pushd "$TEMP_DIR"
  git checkout --orphan "$TARGET_BRANCH"
  git rm -rf .
  touch .nojekyll  # Prevent Jekyll processing
  git add .nojekyll
  git commit -m "Initialize gh-pages branch"
  git push origin "$TARGET_BRANCH"
  popd
}

# Clean existing content and copy new docs
echo "Updating documentation..."
rm -rf "${TEMP_DIR:?}/"*  # Safely clear directory
cp -r "$DOCS_DIR/"* "$TEMP_DIR"
touch "$TEMP_DIR/.nojekyll"  # Add .nojekyll if not exists

# Commit and push changes
pushd "$TEMP_DIR"
git add .
git commit -m "Update core component Javadoc $(date +'%Y-%m-%d %H:%M:%S')"
git push origin "$TARGET_BRANCH"
popd

# Clean up
rm -rf "$TEMP_DIR"

echo ""
echo "Successfully published Javadoc to GitHub Pages!"
echo "Visit: https://$(git config --get remote.origin.url | cut -d':' -f2 | cut -d'.' -f1).github.io/$REPO_NAME/"
