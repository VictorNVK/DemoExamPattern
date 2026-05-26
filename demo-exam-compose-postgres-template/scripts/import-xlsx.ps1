param(
    [string]$SourceDir
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$defaultImportDir = Join-Path $projectRoot "imports"

if (-not (Test-Path (Join-Path $defaultImportDir "Tovar.xlsx"))) {
    $workspaceRoot = Split-Path $projectRoot -Parent
    if (Test-Path (Join-Path $workspaceRoot "Tovar.xlsx")) {
        $defaultImportDir = $workspaceRoot
    }
}

if ([string]::IsNullOrWhiteSpace($SourceDir)) {
    $SourceDir = $defaultImportDir
}

Set-Location $projectRoot
.\gradlew.bat importXlsx -PimportDir="$SourceDir"

