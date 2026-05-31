$ErrorActionPreference = "Stop"

$Script:RootDir = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$Script:BackendDir = Join-Path $RootDir "demo-exam-spring-backend"
$Script:ClientDir = Join-Path $RootDir "demo-exam-compose-postgres-template"

function Write-Log {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] $Message"
}

function Get-BackendPort {
    $propsPath = Join-Path $BackendDir "src\main\resources\application.properties"
    if (-not (Test-Path -LiteralPath $propsPath)) {
        return 8082
    }

    $line = Get-Content -LiteralPath $propsPath | Where-Object {
        $_ -match '^\s*server\.port\s*='
    } | Select-Object -First 1

    if ($line -and $line -match '=\s*(\d+)\s*$') {
        return [int]$Matches[1]
    }

    return 8082
}

function Get-ListeningProcessIds {
    param([int]$Port)

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $connections) {
        return @()
    }

    return @($connections | Select-Object -ExpandProperty OwningProcess -Unique)
}

function Stop-ProcessTree {
    param([int]$ProcessId)

    if ($ProcessId -le 0) {
        return
    }

    $children = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object { $_.ParentProcessId -eq $ProcessId }

    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId $child.ProcessId
    }

    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

function Stop-ProcessesByCommandLine {
    param(
        [string[]]$Patterns,
        [string]$Label
    )

    $stopped = @()

    Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -in @('java.exe', 'javaw.exe') } |
        ForEach-Object {
            $commandLine = $_.CommandLine
            if (-not $commandLine) {
                return
            }

            foreach ($pattern in $Patterns) {
                if ($commandLine -like "*$pattern*") {
                    Write-Log "Stopping $Label (PID $($_.ProcessId)) ..."
                    Stop-ProcessTree -ProcessId $_.ProcessId
                    $stopped += $_.ProcessId
                    break
                }
            }
        }

    return $stopped
}

function Stop-PortListeners {
    param(
        [int]$Port,
        [string]$Label
    )

    $stopped = @()
    foreach ($processId in (Get-ListeningProcessIds -Port $Port)) {
        Write-Log "Stopping $Label on port $Port (PID $processId) ..."
        Stop-ProcessTree -ProcessId $processId
        $stopped += $processId
    }
    return $stopped
}
