# Stop Spring Boot backend (Windows).
# Run from repo root:
#   .\scripts\windows\stop-backend.ps1

. "$PSScriptRoot\common.ps1"

$port = Get-BackendPort
$stopped = @()

Write-Log "Looking for backend on port $port ..."

$stopped += Stop-PortListeners -Port $port -Label "backend"
$stopped += Stop-ProcessesByCommandLine -Label "backend" -Patterns @(
    "demo-exam-spring-backend",
    "DemoExamBackendApplication"
)

$stopped = @($stopped | Select-Object -Unique)

if ($stopped.Count -eq 0) {
    Write-Log "Backend is not running (port $port is free)."
} else {
    Write-Log "Backend stopped."
}
