param(
    [ValidateSet("auto", "docker", "local", "skip")]
    [string]$RedisMode = "auto",
    [switch]$SkipBackend,
    [switch]$SkipFrontend
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "[START] $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[ OK ] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Test-TcpPort {
    param(
        [string]$HostName,
        [int]$Port
    )
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect($HostName, $Port, $null, $null)
        $success = $iar.AsyncWaitHandle.WaitOne(600)
        if (-not $success) {
            $client.Close()
            return $false
        }
        $client.EndConnect($iar)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

function Wait-PortUp {
    param(
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutSeconds = 15
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpPort -HostName $HostName -Port $Port) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }
    return $false
}

function Start-RedisByDocker {
    $dockerExists = $null -ne (Get-Command docker -ErrorAction SilentlyContinue)
    if (-not $dockerExists) {
        return $false
    }

    $containerName = "java-lib-redis"
    $existingContainerId = docker ps -a --filter "name=^/${containerName}$" --format "{{.ID}}"
    if ([string]::IsNullOrWhiteSpace($existingContainerId)) {
        Write-Step "Creating Redis container ($containerName)..."
        docker run -d --name $containerName -p 6379:6379 redis:7-alpine | Out-Null
    } else {
        $runningId = docker ps --filter "name=^/${containerName}$" --format "{{.ID}}"
        if ([string]::IsNullOrWhiteSpace($runningId)) {
            Write-Step "Starting existing Redis container ($containerName)..."
            docker start $containerName | Out-Null
        }
    }

    return (Wait-PortUp -HostName "127.0.0.1" -Port 6379 -TimeoutSeconds 20)
}

function Start-RedisByLocalBinary {
    $redisBatch = "C:\redis\start.bat"
    if (Test-Path -LiteralPath $redisBatch) {
        Write-Step "Starting Redis via C:\redis\start.bat..."
        Start-Process -FilePath $redisBatch -WorkingDirectory "C:\redis" -WindowStyle Minimized | Out-Null
        if (Wait-PortUp -HostName "127.0.0.1" -Port 6379 -TimeoutSeconds 12) {
            return $true
        }
    }

    $redisCommand = Get-Command redis-server -ErrorAction SilentlyContinue
    if ($null -eq $redisCommand) {
        return $false
    }

    Write-Step "Starting local redis-server..."
    Start-Process -FilePath $redisCommand.Source -ArgumentList "--port 6379" -WindowStyle Minimized | Out-Null
    return (Wait-PortUp -HostName "127.0.0.1" -Port 6379 -TimeoutSeconds 10)
}

function Ensure-Redis {
    if ($RedisMode -eq "skip") {
        Write-Warn "Redis startup skipped by parameter."
        return
    }

    if (Test-TcpPort -HostName "127.0.0.1" -Port 6379) {
        Write-Ok "Redis is already running on 127.0.0.1:6379"
        return
    }

    $started = $false

    switch ($RedisMode) {
        "docker" { $started = Start-RedisByDocker }
        "local" { $started = Start-RedisByLocalBinary }
        "auto" {
            $started = Start-RedisByDocker
            if (-not $started) {
                $started = Start-RedisByLocalBinary
            }
        }
    }

    if ($started) {
        Write-Ok "Redis is ready on 127.0.0.1:6379"
    } else {
        throw "Redis failed to start. Use Docker Desktop or install redis-server, then retry."
    }
}

function Ensure-Tooling {
    $missing = @()
    if ($null -eq (Get-Command mvn -ErrorAction SilentlyContinue)) {
        $missing += "mvn"
    }
    if ($null -eq (Get-Command npm -ErrorAction SilentlyContinue)) {
        $missing += "npm"
    }
    if ($missing.Count -gt 0) {
        throw "Missing required tools: $($missing -join ', ')"
    }
}

function Start-Backend {
    Write-Step "Starting Spring Boot backend (new window)..."
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location 'G:\Java-lib'; mvn spring-boot:run"
    ) | Out-Null
    Write-Ok "Backend window launched."
}

function Start-Frontend {
    Write-Step "Starting Vue frontend (new window)..."
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location 'G:\Java-lib'; npm run dev"
    ) | Out-Null
    Write-Ok "Frontend window launched."
}

Write-Step "Checking startup prerequisites..."
Ensure-Tooling
Ensure-Redis

if (-not (Test-TcpPort -HostName "127.0.0.1" -Port 3306)) {
    Write-Warn "MySQL may not be running on 127.0.0.1:3306. Backend startup could fail."
} else {
    Write-Ok "MySQL port 3306 is reachable."
}

if (-not $SkipBackend) {
    Start-Backend
}

if (-not $SkipFrontend) {
    Start-Frontend
}

Write-Host ""
Write-Host "Done. Services launch requested." -ForegroundColor Green
Write-Host "Backend default: http://localhost:8081" -ForegroundColor Gray
Write-Host "Frontend default: http://localhost:3000" -ForegroundColor Gray
