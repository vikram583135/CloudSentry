@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE__=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -body ([IO.File]::ReadAllText('%~f0telerikraquo')) -args $scriptDir}"`) DO @(
  IF "%%A"=="MVN_CMD" (SET __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo.%%A) ELSE (echo.%%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE__%
@SET __MVNW_PSMODULEP_SAVE__=
@SET __MVNW_ARG0_NAME__=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=
@SET MVNW_VERBOSE=
@IF NOT "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*)
@echo Cannot run maven wrapper
@exit /b 1
<# mvnw.cmd wrapper, version 3.2.0
$distributionUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
$distributionSha256Url = ""

function Expand-ZIPFile($file, $destination) {
    $shell = New-Object -ComObject shell.application
    $zip = $shell.NameSpace($file)
    foreach($item in $zip.items()) {
        $shell.NameSpace($destination).CopyHere($item)
    }
}

$scriptDir = $args[0]
$mvnwDir = Join-Path $env:USERPROFILE ".m2\wrapper\dists\apache-maven-3.9.6"
$mvnCmd = Join-Path $mvnwDir "apache-maven-3.9.6\bin\mvn.cmd"

if (!(Test-Path $mvnCmd)) {
    Write-Host "Downloading Maven..."
    $downloadFile = Join-Path $env:TEMP "apache-maven-3.9.6-bin.zip"
    
    if (!(Test-Path $mvnwDir)) {
        New-Item -ItemType Directory -Path $mvnwDir -Force | Out-Null
    }
    
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $distributionUrl -OutFile $downloadFile
    
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $downloadFile -DestinationPath $mvnwDir -Force
    Remove-Item $downloadFile -Force
}

"MVN_CMD=$mvnCmd"
#>
