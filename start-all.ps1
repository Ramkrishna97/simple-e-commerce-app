$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$processFile = "$projectRoot\data\running-processes.json"
if (Test-Path $processFile) {
    & "$projectRoot\stop-all.ps1"
    Start-Sleep -Seconds 2
}
$java17Home = 'C:\Program Files\Java\jdk-17'
if (Test-Path "$java17Home\bin\java.exe") {
    $env:JAVA_HOME = $java17Home
    $env:Path = "$java17Home\bin;$env:Path"
}

$gradleCommand = if (Test-Path 'C:\Program Files\gradle-8.10.2\bin\gradle.bat') { 'C:\Program Files\gradle-8.10.2\bin\gradle.bat' } else { "$projectRoot\gradlew.bat" }
New-Item -ItemType Directory -Path "$projectRoot\data\logs" -Force | Out-Null
$processes = @()
& $gradleCommand --no-daemon bootJar
if ($LASTEXITCODE -ne 0) { throw 'The service jars could not be built. Check the Gradle output above.' }
$serviceDefinitions = @(
    @{ Name = 'auth-service'; Url = 'http://localhost:8081/h2-console' },
    @{ Name = 'user-service'; Url = 'http://localhost:8082/h2-console' },
    @{ Name = 'product-service'; Url = 'http://localhost:8083/api/products?page=0' },
    @{ Name = 'cart-service'; Url = 'http://localhost:8084/h2-console' },
    @{ Name = 'order-service'; Url = 'http://localhost:8085/h2-console' },
    @{ Name = 'notification-service'; Url = 'http://localhost:8086/h2-console' }
)
foreach ($definition in $serviceDefinitions) {
    $service = $definition.Name
    $jar = Get-ChildItem "$projectRoot\$service\build\libs\*.jar" | Where-Object { $_.Name -notlike '*-plain.jar' } | Select-Object -First 1
    if (-not $jar) { throw "No runnable jar was produced for $service." }
    $processes += Start-Process -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList '-jar', $jar.FullName -WorkingDirectory "$projectRoot\$service" -RedirectStandardOutput "$projectRoot\data\logs\$service.out.log" -RedirectStandardError "$projectRoot\data\logs\$service.err.log" -PassThru -WindowStyle Hidden
    $processes | Select-Object Id, ProcessName | ConvertTo-Json | Set-Content $processFile
    $ready = $false
    for ($attempt = 0; $attempt -lt 60 -and -not $ready; $attempt++) {
        try { $ready = (Invoke-WebRequest -UseBasicParsing $definition.Url -TimeoutSec 2).StatusCode -eq 200 } catch { Start-Sleep -Seconds 2 }
    }
    if (-not $ready) { throw "Service did not become ready: $service. Check data\logs for details." }
    Write-Host "$service is ready."
}

$frontendProcess = Start-Process -FilePath 'npm.cmd' -ArgumentList 'start' -WorkingDirectory "$projectRoot\frontend" -RedirectStandardOutput "$projectRoot\data\logs\frontend.out.log" -RedirectStandardError "$projectRoot\data\logs\frontend.err.log" -PassThru
$processes += $frontendProcess
$processes | Select-Object Id, ProcessName | ConvertTo-Json | Set-Content $processFile
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    try { if ((Invoke-WebRequest -UseBasicParsing 'http://localhost:4200' -TimeoutSec 2).StatusCode -eq 200) { break } } catch { Start-Sleep -Seconds 2 }
}
Start-Process 'http://localhost:4200'
Write-Host 'All services are ready. Dashboard: http://localhost:4200'
