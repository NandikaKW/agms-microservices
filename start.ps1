# ============================================
# AGMS - One Command Startup Script
# Run: .\start.ps1
# ============================================

$ErrorActionPreference = "SilentlyContinue"
$ROOT = $PSScriptRoot

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  AGMS - Automated Greenhouse Management    " -ForegroundColor Cyan
Write-Host "  Starting All 7 Microservices...           " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Step 0: Kill any existing Java processes on our ports
Write-Host "[0/7] Stopping old processes..." -ForegroundColor Yellow
Get-Process -Name java 2>$null | Stop-Process -Force 2>$null
Start-Sleep -Seconds 3

# Clean old builds
Write-Host "[0/7] Cleaning old builds..." -ForegroundColor Yellow
Get-ChildItem -Path $ROOT -Include target -Recurse -Directory | Remove-Item -Recurse -Force 2>$null

# Step 1: Config Server (Must start first)
Write-Host ""
Write-Host "[1/7] Starting Config Server (port 8888)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\ConfigServer" -WindowStyle Minimized

# Wait for Config Server to be ready
Write-Host "       Waiting for Config Server..." -ForegroundColor Gray
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8888/actuator/health" -UseBasicParsing -TimeoutSec 2 2>$null
        if ($response.StatusCode -eq 200) { $ready = $true; break }
    } catch {
        # Try the config endpoint instead
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8888/zone-service/default" -UseBasicParsing -TimeoutSec 2 2>$null
            if ($response.StatusCode -eq 200) { $ready = $true; break }
        } catch {}
    }
    Write-Host "." -NoNewline -ForegroundColor Gray
}
if ($ready) {
    Write-Host " READY!" -ForegroundColor Green
} else {
    Write-Host " (Timeout - continuing anyway)" -ForegroundColor Yellow
}

# Step 2: Eureka Server
Write-Host "[2/7] Starting Eureka Server (port 8761)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\Eureka_Sever" -WindowStyle Minimized

# Wait for Eureka
Write-Host "       Waiting for Eureka Server..." -ForegroundColor Gray
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8761" -UseBasicParsing -TimeoutSec 2 2>$null
        if ($response.StatusCode -eq 200) { $ready = $true; break }
    } catch {}
    Write-Host "." -NoNewline -ForegroundColor Gray
}
if ($ready) {
    Write-Host " READY!" -ForegroundColor Green
} else {
    Write-Host " (Timeout - continuing anyway)" -ForegroundColor Yellow
}

# Step 3: API Gateway
Write-Host "[3/7] Starting API Gateway (port 8080)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\Api_Gatway" -WindowStyle Minimized
Start-Sleep -Seconds 10

# Step 4-7: Domain Services (can start in parallel)
Write-Host "[4/7] Starting Zone Service (port 8081)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\ZoneService" -WindowStyle Minimized

Write-Host "[5/7] Starting Sensor Service (port 8082)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\SensorService" -WindowStyle Minimized

Write-Host "[6/7] Starting Automation Service (port 8083)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\AutomationService" -WindowStyle Minimized

Write-Host "[7/7] Starting Crop Service (port 8084)..." -ForegroundColor Green
Start-Process -FilePath "mvn" -ArgumentList "clean spring-boot:run" -WorkingDirectory "$ROOT\CropService" -WindowStyle Minimized

# Final wait for all services
Write-Host ""
Write-Host "All services launched! Waiting 2 minutes for startup..." -ForegroundColor Yellow
Write-Host "(Clean builds take longer the first time)" -ForegroundColor Gray
Start-Sleep -Seconds 120

# Check which ports are alive
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Service Status Check                      " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$services = @(
    @{Name="Config Server";  Port=8888},
    @{Name="Eureka Server";  Port=8761},
    @{Name="API Gateway";    Port=8080},
    @{Name="Zone Service";   Port=8081},
    @{Name="Sensor Service"; Port=8082},
    @{Name="Automation Svc"; Port=8083},
    @{Name="Crop Service";   Port=8084}
)

foreach ($svc in $services) {
    $listening = netstat -ano | findstr "LISTENING" | findstr ":$($svc.Port) "
    if ($listening) {
        Write-Host "  [UP]   $($svc.Name) -> http://localhost:$($svc.Port)" -ForegroundColor Green
    } else {
        Write-Host "  [DOWN] $($svc.Name) -> Port $($svc.Port) not responding" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Open Eureka Dashboard:                    " -ForegroundColor Cyan
Write-Host "  http://localhost:8761                     " -ForegroundColor White
Write-Host ""
Write-Host "  Test via Gateway:                         " -ForegroundColor Cyan
Write-Host "  http://localhost:8080/api/zones           " -ForegroundColor White
Write-Host "  http://localhost:8080/api/sensors/latest  " -ForegroundColor White
Write-Host "  http://localhost:8080/api/automation/logs " -ForegroundColor White
Write-Host "  http://localhost:8080/api/crops           " -ForegroundColor White
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Open Eureka Dashboard automatically
Start-Process "http://localhost:8761"
