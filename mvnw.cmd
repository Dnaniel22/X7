@echo off
setlocal
set "BASE_DIR=%~dp0"
set "PROPS=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%PROPS%" (
  echo Error: no se encontro %PROPS% 1>&2
  exit /b 1
)

set "MAVEN_HOME_RESOLVED="
for /f "usebackq delims=" %%I in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $props=Get-Content -Raw '%PROPS%' ^| ConvertFrom-StringData; $url=$props.distributionUrl; if(-not $url){throw 'distributionUrl no esta definido'}; $file=[IO.Path]::GetFileName($url); $name=[IO.Path]::GetFileNameWithoutExtension($file) -replace '-bin$',''; $m2=if($env:MAVEN_USER_HOME){$env:MAVEN_USER_HOME}else{Join-Path $env:USERPROFILE '.m2'}; $parent=Join-Path $m2 'wrapper\dists'; $home=Join-Path $parent $name; $mvn=Join-Path $home 'bin\mvn.cmd'; if(-not (Test-Path $mvn)){New-Item -ItemType Directory -Force -Path $parent ^| Out-Null; $tmp=Join-Path $env:TEMP $file; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri $url -OutFile $tmp; Expand-Archive -Path $tmp -DestinationPath $parent -Force; Remove-Item $tmp -Force}; Write-Output $home"`) do set "MAVEN_HOME_RESOLVED=%%I"

if not defined MAVEN_HOME_RESOLVED (
  echo Error: no se pudo preparar Maven Wrapper. 1>&2
  exit /b 1
)

call "%MAVEN_HOME_RESOLVED%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
