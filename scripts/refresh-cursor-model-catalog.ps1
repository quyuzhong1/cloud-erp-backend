# Force Cursor to refresh its model catalog cache (does not bypass regional restrictions).
#
# Dry-run:
#   powershell -ExecutionPolicy Bypass -File scripts/refresh-cursor-model-catalog.ps1
#
# Apply (Cursor must be fully closed):
#   powershell -ExecutionPolicy Bypass -File scripts/refresh-cursor-model-catalog.ps1 -Apply

param(
    [switch]$Apply
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pyScript = Join-Path $scriptDir 'refresh-cursor-model-catalog.py'
if (-not (Test-Path $pyScript)) {
    Write-Error "Python script not found: $pyScript"
}

$argsList = @($pyScript)
if ($Apply) {
    $argsList += '--apply'
} else {
    Write-Host 'Dry-run mode. Pass -Apply to write after quitting Cursor.' -ForegroundColor Yellow
}

if ($Apply) {
    $cursor = Get-Process -Name 'Cursor' -ErrorAction SilentlyContinue
    if ($cursor) {
        Write-Error 'Cursor is still running. Quit Cursor completely, then rerun with -Apply.'
    }
}

if (Get-Command py -ErrorAction SilentlyContinue) {
    & py -3 @argsList
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    & python @argsList
} else {
    Write-Error 'Python not found. Install Python 3 and ensure py or python is on PATH.'
}

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
