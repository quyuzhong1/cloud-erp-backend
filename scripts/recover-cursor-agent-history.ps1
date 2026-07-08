# Rebuild Cursor Agent sidebar history from local agent-transcripts (.jsonl).
#
# Usage (dry-run, safe):
#   powershell -ExecutionPolicy Bypass -File scripts/recover-cursor-agent-history.ps1
#
# Apply after fully quitting Cursor:
#   powershell -ExecutionPolicy Bypass -File scripts/recover-cursor-agent-history.ps1 -Apply
#
# Optional:
#   -WorkspacePath 'D:\cloud-erp-backend'
#   -TranscriptsDir 'C:\Users\Administrator\.cursor\projects\d-cloud-erp-backend\agent-transcripts'
#   -ResetMigrationFlags

param(
    [string]$WorkspacePath = 'D:\cloud-erp-backend',
    [string]$TranscriptsDir = '',
    [switch]$Apply,
    [switch]$ResetMigrationFlags
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pyScript = Join-Path $scriptDir 'recover-cursor-agent-history.py'
if (-not (Test-Path $pyScript)) {
    Write-Error "Python script not found: $pyScript"
}

$argsList = @(
    $pyScript
    '--workspace-path'
    $WorkspacePath
)
if ($TranscriptsDir) {
    $argsList += @('--transcripts-dir', $TranscriptsDir)
}
if ($Apply) {
    $argsList += '--apply'
} else {
    Write-Host 'Dry-run mode. Pass -Apply to write after quitting Cursor.' -ForegroundColor Yellow
}
if ($ResetMigrationFlags) {
    $argsList += '--reset-migration-flags'
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

if (-not $Apply) {
    Write-Host ''
    Write-Host 'To apply:' -ForegroundColor Cyan
    Write-Host '  1. Quit Cursor (check Task Manager for Cursor.exe)' -ForegroundColor Cyan
    Write-Host '  2. powershell -ExecutionPolicy Bypass -File scripts/recover-cursor-agent-history.ps1 -Apply' -ForegroundColor Cyan
}
