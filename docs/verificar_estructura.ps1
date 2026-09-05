# =====================================================================
# ALMACAPRINA — Verificación de estructura del proyecto KMP
# =====================================================================
# Uso:
#   .\verificar_estructura.ps1
#   .\verificar_estructura.ps1 -ProjectRoot "C:\repos\almacaprina" -PackagePath "com\didiprogrammer\almacaprina"
# =====================================================================

param(
    [string]$ProjectRoot = "C:\repos\almacaprina",
    [string]$PackagePath = "com\didiprogrammer\almacaprina"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== Verificando estructura del proyecto KMP en $ProjectRoot ===" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $ProjectRoot)) {
    Write-Host "ERROR: No existe la carpeta $ProjectRoot. Revisa la ruta." -ForegroundColor Red
    exit 1
}

$script:ok = @()
$script:issues = @()

function Test-PathReport {
    param($Path, $Description)
    if (Test-Path $Path) {
        $script:ok += "OK: $Description"
        return $true
    } else {
        $script:issues += "FALTA: $Description -> $Path"
        return $false
    }
}

# ---------------------------------------------------------------------
# 1. Archivos raíz esperados del wizard
# ---------------------------------------------------------------------
Test-PathReport "$ProjectRoot\settings.gradle.kts" "settings.gradle.kts" | Out-Null
Test-PathReport "$ProjectRoot\build.gradle.kts" "build.gradle.kts (raíz)" | Out-Null
Test-PathReport "$ProjectRoot\gradle\libs.versions.toml" "Version catalog (gradle\libs.versions.toml)" | Out-Null
Test-PathReport "$ProjectRoot\gradle.properties" "gradle.properties" | Out-Null

# ---------------------------------------------------------------------
# 2. Los 3 módulos reales de este proyecto: shared, androidApp, iosApp
# ---------------------------------------------------------------------
$shared = Join-Path $ProjectRoot "shared"
$androidApp = Join-Path $ProjectRoot "androidApp"
$iosApp = Join-Path $ProjectRoot "iosApp"

Test-PathReport "$shared\build.gradle.kts" "shared\build.gradle.kts" | Out-Null

$commonMainKotlin = "$shared\src\commonMain\kotlin"
$androidMainKotlin = "$shared\src\androidMain\kotlin"
$iosMainKotlin = "$shared\src\iosMain\kotlin"

Test-PathReport $commonMainKotlin "shared\src\commonMain\kotlin" | Out-Null
Test-PathReport $androidMainKotlin "shared\src\androidMain\kotlin" | Out-Null
Test-PathReport $iosMainKotlin "shared\src\iosMain\kotlin" | Out-Null

Test-PathReport "$androidApp\build.gradle.kts" "androidApp\build.gradle.kts (módulo delgado)" | Out-Null
Test-PathReport $iosApp "carpeta iosApp (proyecto Xcode)" | Out-Null

Write-Host "--- Resultado base ---" -ForegroundColor Yellow
$script:ok | ForEach-Object { Write-Host $_ -ForegroundColor Green }
$script:issues | ForEach-Object { Write-Host $_ -ForegroundColor Red }
Write-Host ""

if ($script:issues.Count -gt 0) {
    Write-Host "Faltan piezas de la estructura base. No las creo automáticamente porque" -ForegroundColor Red
    Write-Host "normalmente significa que el proyecto no se generó completo. Revisa manualmente." -ForegroundColor Red
    exit 1
}

# ---------------------------------------------------------------------
# 3. Verificar que el paquete indicado exista; si no, ayudar a detectarlo
# ---------------------------------------------------------------------
$expectedPackageFolder = Join-Path $commonMainKotlin $PackagePath

if (-not (Test-Path $expectedPackageFolder)) {
    Write-Host "AVISO: no encontré el paquete '$PackagePath' dentro de commonMain\kotlin." -ForegroundColor Yellow
    Write-Host "Carpetas que sí existen ahí:" -ForegroundColor Yellow
    Get-ChildItem -Recurse -Directory $commonMainKotlin | ForEach-Object {
        Write-Host "  $($_.FullName.Replace($commonMainKotlin, ''))"
    }
    Write-Host ""
    Write-Host "Vuelve a correr el script con -PackagePath 'com\tu_paquete_real'" -ForegroundColor Yellow
    exit 1
}

Write-Host "=== Paquete '$PackagePath' encontrado. Verificando/creando subcarpetas ===" -ForegroundColor Cyan
Write-Host ""

# ---------------------------------------------------------------------
# 4. Subcarpetas de código que necesitamos en commonMain (ver CLAUDE.md)
# ---------------------------------------------------------------------
$commonSubfolders = @("ui", "domain\model", "domain\repository", "data\remote", "data\mapper", "business")

foreach ($sub in $commonSubfolders) {
    $full = Join-Path $expectedPackageFolder $sub
    if (Test-Path $full) {
        Write-Host "OK: commonMain\...\$sub" -ForegroundColor Green
    } else {
        New-Item -ItemType Directory -Force -Path $full | Out-Null
        Write-Host "CREADO: commonMain\...\$sub" -ForegroundColor Yellow
    }
}

# ---------------------------------------------------------------------
# 5. Paquete base en androidMain e iosMain (para implementaciones actual/)
# ---------------------------------------------------------------------
foreach ($platform in @("androidMain", "iosMain")) {
    $platformPackagePath = Join-Path (Join-Path $shared "src\$platform\kotlin") $PackagePath
    if (Test-Path $platformPackagePath) {
        Write-Host "OK: $platform\...\$PackagePath" -ForegroundColor Green
    } else {
        New-Item -ItemType Directory -Force -Path $platformPackagePath | Out-Null
        Write-Host "CREADO: $platform\...\$PackagePath" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== Listo. Estructura verificada y organizada. ===" -ForegroundColor Cyan
Write-Host "Ya puedes continuar con el Paso 3 (dependencias en shared\build.gradle.kts)." -ForegroundColor Cyan
