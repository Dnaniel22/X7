@echo off
setlocal

set "BASE_DIR=%~dp0"
set "PROPS=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%PROPS%" (
  echo Error: no se encontro %PROPS% 1>&2
  exit /b 1
)

@REM Dos detalles importantes de este bloque:
@REM  - La ruta viaja en la variable de entorno PROPS en vez de incrustarse en
@REM    el comando, para que los espacios del directorio no la partan.
@REM  - No se usa ningun pipe: dentro de un for /f el caret no se desescapa,
@REM    asi que PowerShell recibiria un ^ literal.
set "MAVEN_HOME_RESOLVED="
for /f "usebackq delims=" %%I in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $props=ConvertFrom-StringData (Get-Content -Raw -LiteralPath $env:PROPS); $url=$props.distributionUrl; if(-not $url){throw 'distributionUrl no esta definido'}; $file=[IO.Path]::GetFileName($url); $name=[IO.Path]::GetFileNameWithoutExtension($file) -replace '-bin$',''; $m2=if($env:MAVEN_USER_HOME){$env:MAVEN_USER_HOME}else{Join-Path $env:USERPROFILE '.m2'}; $parent=Join-Path $m2 'wrapper\dists'; $mavenHome=Join-Path $parent $name; $mvn=Join-Path $mavenHome 'bin\mvn.cmd'; if(-not (Test-Path -LiteralPath $mvn)){$null=New-Item -ItemType Directory -Force -Path $parent; $tmp=Join-Path $env:TEMP $file; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri $url -OutFile $tmp; Expand-Archive -LiteralPath $tmp -DestinationPath $parent -Force; Remove-Item -LiteralPath $tmp -Force}; Write-Output $mavenHome"`) do set "MAVEN_HOME_RESOLVED=%%I"

if not defined MAVEN_HOME_RESOLVED (
  echo Error: no se pudo preparar Maven Wrapper. 1>&2
  exit /b 1
)

call "%MAVEN_HOME_RESOLVED%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
