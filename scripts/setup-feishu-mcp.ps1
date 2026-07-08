# Feishu MCP one-time setup: env vars + OAuth (personal app, read group chat e.g. 软件研发组).
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts/setup-feishu-mcp.ps1
#   powershell -ExecutionPolicy Bypass -File scripts/setup-feishu-mcp.ps1 -AppSecret "your_secret"
# Secret is NOT stored in git. Prefer -AppSecret once or pre-set User env FEISHU_APP_SECRET.

param(
    [string]$AppId = 'cli_aac1b895fcb95bb4',
    [string]$AppSecret = ''
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = 'Stop'

if (-not $AppSecret) {
    $AppSecret = [Environment]::GetEnvironmentVariable('FEISHU_APP_SECRET', 'User')
}
if (-not $AppSecret) {
    $AppSecret = [Environment]::GetEnvironmentVariable('FEISHU_APP_SECRET', 'Process')
}
if (-not $AppSecret) {
    $secure = Read-Host 'Enter Feishu App Secret (input hidden, from open.feishu.cn app credentials page)' -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { $AppSecret = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
if (-not $AppSecret) {
    Write-Error 'App Secret is required. Pass -AppSecret or set FEISHU_APP_SECRET (User env).'
}

[Environment]::SetEnvironmentVariable('FEISHU_APP_ID', $AppId, 'User')
[Environment]::SetEnvironmentVariable('FEISHU_APP_SECRET', $AppSecret, 'User')

$defaultChatName = -join (0x8F6F, 0x4EF6, 0x7814, 0x53D1, 0x7EC4 | ForEach-Object { [char]$_ })
if (-not [Environment]::GetEnvironmentVariable('FEISHU_OKR_CHAT_NAME', 'User')) {
    [Environment]::SetEnvironmentVariable('FEISHU_OKR_CHAT_NAME', $defaultChatName, 'User')
}

Write-Host "FEISHU_APP_ID set (User): $AppId" -ForegroundColor Green
Write-Host 'FEISHU_APP_SECRET set (User): ***' -ForegroundColor Green
Write-Host "FEISHU_OKR_CHAT_NAME: $([Environment]::GetEnvironmentVariable('FEISHU_OKR_CHAT_NAME','User'))" -ForegroundColor Green

Write-Host ''
Write-Host 'Before OAuth, ensure in YOUR app admin (you should have access):' -ForegroundColor Yellow
Write-Host "  https://open.feishu.cn/app/$AppId/safe" -ForegroundColor Yellow
Write-Host '  1) Redirect URL: http://localhost:3000/callback' -ForegroundColor Yellow
Write-Host '  2) Permissions (user): IM (im:*) + docx:document:readonly + wiki:wiki:readonly' -ForegroundColor Yellow
Write-Host '  3) Test users + publish version (availability test OK)' -ForegroundColor Yellow
Write-Host '  Note: drive:drive:readonly NOT required for Wiki/docx links' -ForegroundColor Yellow
Write-Host ''
Write-Host 'Starting OAuth (open URL in browser within 60 seconds)...' -ForegroundColor Cyan

$env:FEISHU_APP_ID = $AppId
$env:FEISHU_APP_SECRET = $AppSecret
$imScopes = 'im:chat:readonly im:message im:message.group_msg im:message.group_msg:get_as_user im:message:readonly docx:document:readonly wiki:wiki:readonly offline_access'
npx -y @larksuiteoapi/lark-mcp login -a $AppId -s $AppSecret --scope $imScopes

Write-Host ''
Write-Host 'Done. Fully restart Cursor, enable lark-mcp in Settings -> MCP.' -ForegroundColor Cyan
