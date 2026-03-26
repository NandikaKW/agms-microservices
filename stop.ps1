# ============================================
# AGMS - Stop All Services
# Run: .\stop.ps1
# ============================================

Write-Host ""
Write-Host "Stopping all AGMS services..." -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2
Write-Host "All services stopped!" -ForegroundColor Green
Write-Host ""
