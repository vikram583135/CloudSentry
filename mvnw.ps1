# Maven Wrapper PowerShell Script
# Downloads and runs Maven automatically

$mavenVersion = "3.9.6"
$mavenDir = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-$mavenVersion"
$mavenBin = "$mavenDir\apache-maven-$mavenVersion\bin\mvn.cmd"

if (!(Test-Path $mavenBin)) {
    Write-Host "Maven not found. Downloading Maven $mavenVersion..." -ForegroundColor Yellow
    
    $downloadUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
    $downloadFile = "$env:TEMP\apache-maven-$mavenVersion-bin.zip"
    
    # Create directory
    if (!(Test-Path $mavenDir)) {
        New-Item -ItemType Directory -Path $mavenDir -Force | Out-Null
    }
    
    # Download Maven
    Write-Host "Downloading from $downloadUrl..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $downloadUrl -OutFile $downloadFile
    
    # Extract
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $downloadFile -DestinationPath $mavenDir -Force
    
    # Cleanup
    Remove-Item $downloadFile -Force
    
    Write-Host "Maven installed successfully!" -ForegroundColor Green
}

# Run Maven with passed arguments
& $mavenBin $args
