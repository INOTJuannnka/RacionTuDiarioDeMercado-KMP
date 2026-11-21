# ============================================================================
#  Racion - Tu Diario de Mercado  (Compose Multiplatform)
#  Lanzador portable para Windows.
#
#  Uso:  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\racion.ps1 desktop
#        powershell -NoProfile -ExecutionPolicy Bypass -File scripts\racion.ps1 android
#
#  No tiene rutas fijas: busca el JDK y el SDK de Android en esta maquina.
# ============================================================================
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('desktop', 'android')]
    [string]$Target = 'desktop'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$gradlew = Join-Path $root 'gradlew.bat'

function Write-Head([string]$text) {
    Write-Host ''
    Write-Host '============================================================' -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host '============================================================' -ForegroundColor Cyan
}

# ---------------------------------------------------------------------------
# JDK: Gradle 8.9 solo acepta Java 17..22 (Java 25 falla)
# ---------------------------------------------------------------------------
function Get-JavaMajor {
    param([string]$JdkHome, [string]$JavaExe)

    # 1) Camino preferido: el archivo "release" del JDK (JAVA_VERSION="21.0.11").
    #    Evita ejecutar java.exe, que en PowerShell 5.1 escribe en stderr y con
    #    ErrorActionPreference='Stop' se convierte en excepcion terminante.
    $release = Join-Path $JdkHome 'release'
    if (Test-Path -LiteralPath $release) {
        foreach ($line in (Get-Content -LiteralPath $release -ErrorAction SilentlyContinue)) {
            if ($line -match '^JAVA_VERSION="(\d+)(?:\.(\d+))?') {
                $major = [int]$Matches[1]
                if ($major -eq 1 -and $Matches[2]) { $major = [int]$Matches[2] }
                return $major
            }
        }
    }

    # 2) Fallback: ejecutar java y parsear la salida.
    if (-not (Test-Path -LiteralPath $JavaExe)) { return $null }
    $previous = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { $out = (& $JavaExe -version 2>&1 | Out-String) }
    catch { return $null }
    finally { $ErrorActionPreference = $previous }

    $m = [regex]::Match($out, '"(\d+)(?:\.(\d+))?')
    if (-not $m.Success) { return $null }
    $major = [int]$m.Groups[1].Value
    if ($major -eq 1 -and $m.Groups[2].Success) { $major = [int]$m.Groups[2].Value }
    return $major
}

function Get-JdkCandidates {
    $list = New-Object 'System.Collections.Generic.List[string]'

    if ($env:JAVA_HOME) { $list.Add($env:JAVA_HOME) }
    if ($env:JDK_HOME)  { $list.Add($env:JDK_HOME) }

    $roots = @()
    if ($env:USERPROFILE)  { $roots += (Join-Path $env:USERPROFILE  '.jdks') }
    if ($env:LOCALAPPDATA) { $roots += (Join-Path $env:LOCALAPPDATA 'Programs') }
    $roots += @(
        'C:\Program Files\Java',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Amazon Corretto',
        'C:\Program Files\Zulu',
        'C:\Program Files\BellSoft',
        'C:\Program Files\Semeru',
        'C:\Program Files\RedHat'
    )

    foreach ($r in $roots) {
        if (Test-Path -LiteralPath $r) {
            Get-ChildItem -LiteralPath $r -Directory -ErrorAction SilentlyContinue |
                ForEach-Object { $list.Add($_.FullName) }
        }
    }

    # Ultimo recurso: el JBR que trae Android Studio (puede ser Java 25 -> se descarta)
    $studioJbr = 'C:\Program Files\Android\Android Studio\jbr'
    if (Test-Path -LiteralPath $studioJbr) { $list.Add($studioJbr) }

    return $list
}

function Resolve-Jdk {
    $seen  = @{}
    $found = @()

    foreach ($candidate in (Get-JdkCandidates)) {
        if (-not $candidate) { continue }
        $jdkHome = $candidate.TrimEnd('\')
        if ($seen.ContainsKey($jdkHome.ToLowerInvariant())) { continue }
        $seen[$jdkHome.ToLowerInvariant()] = $true

        $exe = Join-Path $jdkHome 'bin\java.exe'
        if (-not (Test-Path -LiteralPath $exe)) { continue }

        $major = Get-JavaMajor -JdkHome $jdkHome -JavaExe $exe
        if ($null -eq $major) { continue }
        if ($major -lt 17 -or $major -gt 22) { continue }

        $pref = switch ($major) { 21 { 0 } 22 { 1 } 20 { 2 } 19 { 3 } 18 { 4 } default { 5 } }
        $found += [pscustomobject]@{ Home = $jdkHome; Major = $major; Pref = $pref }
    }

    if ($found.Count -eq 0) { return $null }
    return ($found | Sort-Object -Property Pref, Major | Select-Object -First 1)
}

# ---------------------------------------------------------------------------
# Android SDK
# ---------------------------------------------------------------------------
function Resolve-AndroidSdk {
    $candidates = @()
    if ($env:ANDROID_HOME)     { $candidates += $env:ANDROID_HOME }
    if ($env:ANDROID_SDK_ROOT) { $candidates += $env:ANDROID_SDK_ROOT }
    if ($env:LOCALAPPDATA)     { $candidates += (Join-Path $env:LOCALAPPDATA 'Android\Sdk') }

    $props = Join-Path $root 'local.properties'
    if (Test-Path -LiteralPath $props) {
        foreach ($line in (Get-Content -LiteralPath $props)) {
            if ($line -match '^\s*sdk\.dir\s*=\s*(.+?)\s*$') {
                $value = $Matches[1].Trim()
                $value = $value -replace '\\\\', '\'
                $value = $value -replace '\\:', ':'
                $candidates += $value
            }
        }
    }

    # Preferir uno que tenga adb
    foreach ($c in $candidates) {
        if ($c -and (Test-Path -LiteralPath (Join-Path $c 'platform-tools\adb.exe'))) { return $c }
    }
    foreach ($c in $candidates) {
        if ($c -and (Test-Path -LiteralPath $c)) { return $c }
    }
    return $null
}

function Fail([string]$message) {
    Write-Host ''
    Write-Host "[ERROR] $message" -ForegroundColor Red
    exit 1
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
$label = if ($Target -eq 'desktop') { 'Desktop (Windows)' } else { 'Android' }
Write-Head "Racion - $label"

if (-not (Test-Path -LiteralPath $gradlew)) {
    Fail "No se encontro gradlew.bat en '$root'. Ejecuta este script desde la carpeta del proyecto."
}

$jdk = Resolve-Jdk
if (-not $jdk) {
    Write-Host ''
    Write-Host 'No se encontro un JDK compatible (se necesita Java 17, 18, 19, 20, 21 o 22).' -ForegroundColor Yellow
    Write-Host ''
    Write-Host 'Gradle 8.9 NO soporta Java 25, que es el que trae Android Studio por defecto.'
    Write-Host ''
    Write-Host 'Solucion: instala Temurin 21 (gratis, sin cuenta):'
    Write-Host '  https://adoptium.net/temurin/releases/?version=21'
    Write-Host ''
    Write-Host 'O si ya lo tenes instalado, defini JAVA_HOME antes de correr:'
    Write-Host '  setx JAVA_HOME "C:\ruta\a\tu\jdk-21"'
    Write-Host '  (despues cerra y volve a abrir la terminal)'
    Fail 'JDK 17-22 no encontrado.'
}

$env:JAVA_HOME = $jdk.Home
$env:Path = (Join-Path $jdk.Home 'bin') + ';' + $env:Path
Write-Host ("JDK {0}  ->  {1}" -f $jdk.Major, $jdk.Home) -ForegroundColor Green

if ($Target -eq 'desktop') {
    Write-Host ''
    Write-Host 'Compilando y abriendo la ventana de Racion...'
    Write-Host '(La primera vez puede tardar varios minutos: Gradle descarga dependencias)'
    Write-Host ''

    Push-Location $root
    try {
        & $gradlew :composeApp:run
        $code = $LASTEXITCODE
    }
    finally { Pop-Location }

    Write-Host ''
    if ($code -eq 0) { Write-Host 'La ventana de Racion se cerro normalmente.' -ForegroundColor Green }
    else             { Write-Host "[ERROR] Gradle termino con codigo $code." -ForegroundColor Red }
    exit $code
}

# ------------------------------- Android -----------------------------------
$sdk = Resolve-AndroidSdk
if (-not $sdk) {
    Write-Host ''
    Write-Host 'No se encontro el SDK de Android.' -ForegroundColor Yellow
    Write-Host 'Instala Android Studio (incluye el SDK) o defini ANDROID_HOME.'
    Write-Host 'Alternativa: crea local.properties en la raiz con:'
    Write-Host '  sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk'
    Fail 'SDK de Android no encontrado.'
}

Write-Host "Android SDK  ->  $sdk" -ForegroundColor Green

Push-Location $root
try {
    Write-Host ''
    Write-Host 'Compilando el APK...'
    & $gradlew :composeApp:assembleDebug
    if ($LASTEXITCODE -ne 0) { Fail "Fallo la compilacion del APK (codigo $LASTEXITCODE)." }

    $apk = Join-Path $root 'composeApp\build\outputs\apk\debug\composeApp-debug.apk'
    if (-not (Test-Path -LiteralPath $apk)) { Fail "No se genero el APK en '$apk'." }

    $adb = Join-Path $sdk 'platform-tools\adb.exe'
    if (-not (Test-Path -LiteralPath $adb)) { Fail "No se encontro adb en '$adb'." }

    Write-Host ''
    Write-Host '--- Dispositivos conectados ---'
    & $adb devices

    Write-Host ''
    Write-Host 'Instalando el APK...'
    & $adb install -r $apk
    if ($LASTEXITCODE -ne 0) {
        Write-Host ''
        Write-Host 'No se pudo instalar. Revisa que:' -ForegroundColor Yellow
        Write-Host '  1. El emulador este encendido (o el telefono con depuracion USB).'
        Write-Host '  2. La lista de arriba muestre un dispositivo como "device" y no "offline".'
        Fail "Fallo la instalacion (codigo $LASTEXITCODE)."
    }

    Write-Host ''
    Write-Host 'Abriendo Racion...'
    & $adb shell am start -n com.racion.app/.MainActivity
    $code = $LASTEXITCODE
}
finally { Pop-Location }

Write-Host ''
if ($code -eq 0) { Write-Host 'Listo: Racion deberia estar abierta en el dispositivo.' -ForegroundColor Green }
else             { Write-Host "[ERROR] No se pudo iniciar la app (codigo $code)." -ForegroundColor Red }
exit $code
