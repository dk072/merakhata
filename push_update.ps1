param (
    [string]$ReleaseNotes = "🚀 New features and performance improvements added!"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Mera Khata - Auto Push OTA Update Script  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Set Java Environment
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# 2. Read update.json
$jsonPath = Join-Path $PSScriptRoot "update.json"
if (-not (Test-Path $jsonPath)) {
    Write-Host "Error: update.json not found!" -ForegroundColor Red
    exit 1
}

$jsonContent = Get-Content $jsonPath -Raw | ConvertFrom-Json
$currentCode = [int]$jsonContent.versionCode
$newCode = $currentCode + 1

# Parse current version name (e.g. 1.0.2 -> 1.0.3)
$versionParts = $jsonContent.versionName.Split('.')
if ($versionParts.Count -eq 3) {
    $patch = [int]$versionParts[2] + 1
    $newName = "$($versionParts[0]).$($versionParts[1]).$patch"
} else {
    $newName = "1.0.$newCode"
}

Write-Host "Updating version: v$($jsonContent.versionName) (Build $currentCode) -> v$newName (Build $newCode)" -ForegroundColor Yellow

# 3. Update build.gradle.kts versionCode & versionName
$gradleFile = Join-Path $PSScriptRoot "app\build.gradle.kts"
$gradleContent = Get-Content $gradleFile -Raw
$gradleContent = $gradleContent -replace "versionCode = \d+", "versionCode = $newCode"
$gradleContent = $gradleContent -replace 'versionName = "[^"]+"', "versionName = `"$newName`""
Set-Content -Path $gradleFile -Value $gradleContent

# 4. Build fresh APK
Write-Host "Building APK..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug

$builtApk = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $builtApk)) {
    Write-Host "Build failed: APK not found at $builtApk" -ForegroundColor Red
    exit 1
}

# 5. Copy APK to root for distribution
$targetApkName = "MeraKhata_v${newName}.apk"
$targetApkPath = Join-Path $PSScriptRoot $targetApkName
Copy-Item -Path $builtApk -Destination $targetApkPath -Force
Write-Host "APK created: $targetApkName" -ForegroundColor Green

# 6. Update update.json
$jsonContent.versionCode = $newCode
$jsonContent.versionName = $newName
$jsonContent.releaseNotes = $ReleaseNotes
# Update apkUrl matching target repo/server URL if configured
if ($jsonContent.apkUrl -match "/[^/]+\.apk$") {
    $jsonContent.apkUrl = $jsonContent.apkUrl -replace "/[^/]+\.apk$", "/$targetApkName"
}

$updatedJson = $jsonContent | ConvertTo-Json -Depth 5
Set-Content -Path $jsonPath -Value $updatedJson
Write-Host "update.json updated successfully!" -ForegroundColor Green

# 7. Git commit & push if git repository exists
if (Test-Path (Join-Path $PSScriptRoot ".git")) {
    Write-Host "Committing and pushing to Git..." -ForegroundColor Cyan
    git add .
    git commit -m "release: Push OTA Update v$newName (Build $newCode)"
    git push
    Write-Host "Successfully pushed to server!" -ForegroundColor Green
} else {
    Write-Host "Note: Git repository not yet linked to a remote. Run 'git init' and link to GitHub to enable 1-click push." -ForegroundColor Yellow
}

Write-Host "==========================================" -ForegroundColor Green
Write-Host "  OTA Update v$newName Published Successfully! " -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
