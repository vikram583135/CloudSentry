# ==============================================================================
# CloudSentry — Cloudflare Pages 1-Click Deployment Script
# ==============================================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  ☁️ CloudSentry -> Cloudflare Deployment  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$distDir = "src\main\resources\static"

if (!(Test-Path $distDir)) {
    Write-Host "Error: Static directory '$distDir' not found." -ForegroundColor Red
    exit 1
}

Write-Host "Deploying directory: $distDir" -ForegroundColor Green
Write-Host "Checking Wrangler CLI..." -ForegroundColor Yellow

# Run wrangler pages deploy
npx --yes wrangler pages deploy $distDir --project-name cloudsentry

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n Deployment to Cloudflare Pages completed successfully!" -ForegroundColor Green
} else {
    Write-Host "`n Wrangler exited with code $LASTEXITCODE. If you are not logged in, run 'npx wrangler login' first." -ForegroundColor Yellow
}
