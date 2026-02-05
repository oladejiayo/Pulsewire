<#
.SYNOPSIS
    PulseWire Application Startup Script
.DESCRIPTION
    Builds and runs the complete PulseWire market data platform including:
    - Infrastructure (PostgreSQL, Kafka via Docker Compose)
    - Control Plane API
    - Data Plane (Feed Adapters, Normalizer, WebSocket Gateway)
    - Frontend (React admin dashboard)
.PARAMETER Mode
    Startup mode: 'full' (default), 'local', 'infra-only', 'build-only'
    - full: Start Docker infrastructure + all services
    - local: Start services with in-memory backbone (no Docker)
    - infra-only: Start only Docker infrastructure
    - build-only: Just build the project
.PARAMETER SkipBuild
    Skip Maven build step
.PARAMETER Clean
    Perform clean build
.PARAMETER NoFrontend
    Skip starting the frontend dev server
.EXAMPLE
    .\start-pulsewire.ps1
    .\start-pulsewire.ps1 -Mode local
    .\start-pulsewire.ps1 -Mode full -Clean
    .\start-pulsewire.ps1 -NoFrontend
#>

param(
    [ValidateSet('full', 'local', 'infra-only', 'build-only')]
    [string]$Mode = 'local',
    [switch]$SkipBuild,
    [switch]$Clean,
    [switch]$NoFrontend
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot

# Colors for output
function Write-Header { param([string]$Message) Write-Host "`n=== $Message ===" -ForegroundColor Cyan }
function Write-Success { param([string]$Message) Write-Host "[OK] $Message" -ForegroundColor Green }
function Write-Info { param([string]$Message) Write-Host "[INFO] $Message" -ForegroundColor Yellow }
function Write-Error { param([string]$Message) Write-Host "[ERROR] $Message" -ForegroundColor Red }

# Banner
Write-Host @"

  ____        _          __        ___           
 |  _ \ _   _| |___  ___\ \      / (_)_ __ ___  
 | |_) | | | | / __|/ _ \\ \ /\ / /| | '__/ _ \ 
 |  __/| |_| | \__ \  __/ \ V  V / | | | |  __/ 
 |_|    \__,_|_|___/\___|  \_/\_/  |_|_|  \___| 
                                                 
  Market Data Event Fabric Platform
  
"@ -ForegroundColor Magenta

Write-Info "Starting PulseWire in '$Mode' mode..."

# Check prerequisites
Write-Header "Checking Prerequisites"

# Check Java
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($null -eq $javaCmd) {
    # Try checking JAVA_HOME
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        $javaCmd = "$env:JAVA_HOME\bin\java.exe"
        Write-Success "Java found via JAVA_HOME: $env:JAVA_HOME"
    } else {
        Write-Error "Java not found. Please install Java 21+ and ensure it's in PATH or JAVA_HOME is set"
        exit 1
    }
} else {
    $javaVersionOutput = & java --version 2>&1 | Select-Object -First 1
    Write-Success "Java found: $javaVersionOutput"
}

# Check Maven
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -eq $mvnCmd) {
    # Try checking M2_HOME or MAVEN_HOME
    $mavenHome = $env:M2_HOME
    if (-not $mavenHome) { $mavenHome = $env:MAVEN_HOME }
    if ($mavenHome -and (Test-Path "$mavenHome\bin\mvn.cmd")) {
        Write-Success "Maven found via M2_HOME/MAVEN_HOME: $mavenHome"
    } else {
        Write-Error "Maven not found. Please install Maven 3.8+ and ensure it's in PATH"
        exit 1
    }
} else {
    $mvnVersionOutput = & mvn -version 2>&1 | Select-Object -First 1
    Write-Success "Maven found: $mvnVersionOutput"
}

# Check Docker (for full/infra-only modes)
if ($Mode -in @('full', 'infra-only')) {
    $dockerCmd = Get-Command docker -ErrorAction SilentlyContinue
    if ($null -eq $dockerCmd) {
        Write-Error "Docker not found. Please install Docker Desktop or use '-Mode local'"
        exit 1
    } else {
        $dockerVersionOutput = & docker --version 2>&1
        Write-Success "Docker found: $dockerVersionOutput"
    }
}

# Check Node.js (for frontend)
$nodeAvailable = $false
if (-not $NoFrontend) {
    $nodeCmd = Get-Command node -ErrorAction SilentlyContinue
    if ($null -eq $nodeCmd) {
        Write-Info "Node.js not found. Frontend will not be started. Install Node.js 18+ or use -NoFrontend flag."
    } else {
        $nodeVersionOutput = & node --version 2>&1
        Write-Success "Node.js found: $nodeVersionOutput"
        $nodeAvailable = $true
        
        # Check if frontend dependencies are installed
        $frontendPath = Join-Path $ProjectRoot "pulsewire-frontend"
        $nodeModulesPath = Join-Path $frontendPath "node_modules"
        if (-not (Test-Path $nodeModulesPath)) {
            Write-Info "Installing frontend dependencies..."
            Push-Location $frontendPath
            npm install
            Pop-Location
        }
    }
}

# Build project
if (-not $SkipBuild) {
    Write-Header "Building Project"
    
    Push-Location $ProjectRoot
    try {
        if ($Clean) {
            Write-Info "Performing clean build..."
            mvn clean install -DskipTests -q
        } else {
            Write-Info "Building project..."
            mvn install -DskipTests -q
        }
        
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Maven build failed"
            exit 1
        }
        Write-Success "Build completed successfully"
    } finally {
        Pop-Location
    }
}

if ($Mode -eq 'build-only') {
    Write-Success "Build complete. Exiting."
    exit 0
}

# Start infrastructure (Docker)
if ($Mode -in @('full', 'infra-only')) {
    Write-Header "Starting Infrastructure (Docker Compose)"
    
    Push-Location $ProjectRoot
    try {
        # Check if containers are already running
        $running = docker-compose ps -q 2>$null
        if ($running) {
            Write-Info "Stopping existing containers..."
            docker-compose down
        }
        
        Write-Info "Starting PostgreSQL, Zookeeper, and Kafka..."
        docker-compose up -d postgres zookeeper kafka
        
        # Wait for Kafka to be ready
        Write-Info "Waiting for Kafka to be ready..."
        $maxAttempts = 30
        $attempt = 0
        do {
            Start-Sleep -Seconds 2
            $attempt++
            $kafkaReady = docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Kafka is ready"
                break
            }
            Write-Info "Waiting for Kafka... ($attempt/$maxAttempts)"
        } while ($attempt -lt $maxAttempts)
        
        if ($attempt -ge $maxAttempts) {
            Write-Error "Kafka failed to start in time"
            exit 1
        }
        
        # Create Kafka topics
        Write-Info "Creating Kafka topics..."
        docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic raw.trades --partitions 3 --replication-factor 1
        docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic raw.quotes --partitions 3 --replication-factor 1
        docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic canonical.events --partitions 3 --replication-factor 1
        Write-Success "Kafka topics created"
        
    } finally {
        Pop-Location
    }
}

if ($Mode -eq 'infra-only') {
    Write-Success "Infrastructure started. Exiting."
    Write-Info "PostgreSQL: localhost:5432"
    Write-Info "Kafka: localhost:9092"
    exit 0
}

# Start services
Write-Header "Starting PulseWire Services"

# Determine profile
$profile = if ($Mode -eq 'full') { 'docker' } else { 'inmemory' }
Write-Info "Using profile: $profile"

# Start Control Plane in background
Write-Info "Starting Control Plane API..."
$controlPlaneJob = Start-Job -ScriptBlock {
    param($root, $profile)
    Set-Location $root
    if ($profile -eq 'docker') {
        mvn -pl pulsewire-control-plane spring-boot:run "-Dspring-boot.run.profiles=$profile" 2>&1
    } else {
        mvn -pl pulsewire-control-plane spring-boot:run 2>&1
    }
} -ArgumentList $ProjectRoot, $profile

# Wait for Control Plane to start
Write-Info "Waiting for Control Plane to start..."
$maxAttempts = 30
$attempt = 0
do {
    Start-Sleep -Seconds 2
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Success "Control Plane is ready at http://localhost:8080"
            break
        }
    } catch {
        Write-Info "Waiting for Control Plane... ($attempt/$maxAttempts)"
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -ge $maxAttempts) {
    Write-Error "Control Plane failed to start. Check logs:"
    Receive-Job -Job $controlPlaneJob
    exit 1
}

# Start Data Plane in background
Write-Info "Starting Data Plane (Feed Adapters, Normalizer, WebSocket Gateway)..."
$dataPlaneJob = Start-Job -ScriptBlock {
    param($root, $profile)
    Set-Location $root
    if ($profile -eq 'docker') {
        mvn -pl pulsewire-data-plane spring-boot:run "-Dspring-boot.run.profiles=kafka" 2>&1
    } else {
        mvn -pl pulsewire-data-plane spring-boot:run "-Dspring-boot.run.profiles=inmemory" 2>&1
    }
} -ArgumentList $ProjectRoot, $profile

# Wait for Data Plane to start
Write-Info "Waiting for Data Plane to start..."
$attempt = 0
do {
    Start-Sleep -Seconds 2
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Success "Data Plane is ready at http://localhost:8081"
            break
        }
    } catch {
        Write-Info "Waiting for Data Plane... ($attempt/$maxAttempts)"
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -ge $maxAttempts) {
    Write-Error "Data Plane failed to start. Check logs:"
    Receive-Job -Job $dataPlaneJob
    exit 1
}

# Start Frontend (if Node.js available)
$frontendJob = $null
if ($nodeAvailable -and -not $NoFrontend) {
    Write-Info "Starting Frontend dev server..."
    $frontendJob = Start-Job -ScriptBlock {
        param($root)
        Set-Location (Join-Path $root "pulsewire-frontend")
        npm run dev 2>&1
    } -ArgumentList $ProjectRoot
    
    # Wait for Frontend to start
    Write-Info "Waiting for Frontend to start..."
    $attempt = 0
    do {
        Start-Sleep -Seconds 2
        $attempt++
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Success "Frontend is ready at http://localhost:3000"
                break
            }
        } catch {
            Write-Info "Waiting for Frontend... ($attempt/$maxAttempts)"
        }
    } while ($attempt -lt $maxAttempts)
    
    if ($attempt -ge $maxAttempts) {
        Write-Info "Frontend may still be starting. Check http://localhost:3000"
    }
}

# Final summary
Write-Header "PulseWire Started Successfully!"
Write-Host ""
Write-Host "  Services:" -ForegroundColor White
Write-Host "    Control Plane API:    http://localhost:8080" -ForegroundColor Green
Write-Host "    Data Plane:           http://localhost:8081" -ForegroundColor Green
Write-Host "    WebSocket Gateway:    ws://localhost:8081/ws/market-data" -ForegroundColor Green
if ($nodeAvailable -and -not $NoFrontend) {
    Write-Host "    Frontend Dashboard:   http://localhost:3000" -ForegroundColor Green
}

if ($Mode -eq 'full') {
    Write-Host ""
    Write-Host "  Infrastructure:" -ForegroundColor White
    Write-Host "    PostgreSQL:           localhost:5432" -ForegroundColor Green
    Write-Host "    Kafka:                localhost:9092" -ForegroundColor Green
}

Write-Host ""
Write-Host "  API Endpoints:" -ForegroundColor White
Write-Host "    GET  /api/instruments     - List instruments" -ForegroundColor Gray
Write-Host "    POST /api/instruments     - Create instrument" -ForegroundColor Gray
Write-Host "    GET  /api/feeds           - List feeds" -ForegroundColor Gray
Write-Host "    POST /api/feeds           - Create feed" -ForegroundColor Gray
Write-Host "    GET  /api/subscriptions   - List subscriptions" -ForegroundColor Gray
Write-Host "    POST /api/subscriptions   - Create subscription" -ForegroundColor Gray
Write-Host ""
Write-Host "  Press Ctrl+C to stop all services" -ForegroundColor Yellow
Write-Host ""

# Keep script running and forward logs
try {
    while ($true) {
        # Check if jobs are still running
        if ($controlPlaneJob.State -eq 'Failed') {
            Write-Error "Control Plane crashed:"
            Receive-Job -Job $controlPlaneJob
            break
        }
        if ($dataPlaneJob.State -eq 'Failed') {
            Write-Error "Data Plane crashed:"
            Receive-Job -Job $dataPlaneJob
            break
        }
        if ($frontendJob -and $frontendJob.State -eq 'Failed') {
            Write-Error "Frontend crashed:"
            Receive-Job -Job $frontendJob
            break
        }
        Start-Sleep -Seconds 5
    }
} finally {
    Write-Header "Shutting Down"
    
    Write-Info "Stopping services..."
    Stop-Job -Job $controlPlaneJob -ErrorAction SilentlyContinue
    Stop-Job -Job $dataPlaneJob -ErrorAction SilentlyContinue
    Remove-Job -Job $controlPlaneJob -Force -ErrorAction SilentlyContinue
    Remove-Job -Job $dataPlaneJob -Force -ErrorAction SilentlyContinue
    
    if ($frontendJob) {
        Stop-Job -Job $frontendJob -ErrorAction SilentlyContinue
        Remove-Job -Job $frontendJob -Force -ErrorAction SilentlyContinue
    }
    
    if ($Mode -eq 'full') {
        Write-Info "Stopping Docker containers..."
        Push-Location $ProjectRoot
        docker-compose down
        Pop-Location
    }
    
    Write-Success "PulseWire stopped"
}
