# Stop backend and client (Windows).
# Run from repo root:
#   .\scripts\windows\stop-all.ps1

. "$PSScriptRoot\common.ps1"

& "$PSScriptRoot\stop-backend.ps1"
& "$PSScriptRoot\stop-client.ps1"

Write-Log "Done."
