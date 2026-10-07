#!/usr/bin/env bash
# ==============================================================================
# CloudSentry — Cloudflare Pages 1-Click Deployment Script (Bash)
# ==============================================================================

set -e

echo "=========================================="
echo "  ☁️ CloudSentry -> Cloudflare Deployment  "
echo "=========================================="

DIST_DIR="src/main/resources/static"

if [ ! -d "$DIST_DIR" ]; then
    echo "Error: Directory '$DIST_DIR' not found."
    exit 1
fi

echo "Deploying directory: $DIST_DIR"
echo "Running Wrangler..."

npx --yes wrangler pages deploy "$DIST_DIR" --project-name cloudsentry

echo "✅ Cloudflare deployment completed!"
