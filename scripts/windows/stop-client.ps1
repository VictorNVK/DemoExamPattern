# Stop Compose Desktop client (Windows).
# Run from repo root:
#   .\scripts\windows\stop-client.ps1

. "$PSScriptRoot\common.ps1"

$stopped = Stop-ProcessesByCommandLine -Label "client" -Patterns @(
    "demo-exam-compose-postgres-template",
    "ru.demoexam.template.MainKt",
    "DemoExamTemplate"
)

if ($stopped.Count -eq 0) {
    Write-Log "Client process not found."
} else {
    Write-Log "Client stopped."
}
