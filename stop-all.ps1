$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$processFile = "$projectRoot\data\running-processes.json"
if (Test-Path $processFile) {
    $processes = Get-Content $processFile -Raw | ConvertFrom-Json
    foreach ($process in @($processes)) {
        if (Get-Process -Id $process.Id -ErrorAction SilentlyContinue) {
            & taskkill.exe /PID $process.Id /T /F | Out-Null
        }
    }
    Remove-Item -LiteralPath $processFile -Force
}
Write-Host 'Marketplace services and dashboard stopped.'
