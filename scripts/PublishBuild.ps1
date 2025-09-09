$BucketName = "blockgame-assets"

# Location of the secret file (script dir fallback -> current dir)
$scriptDir = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
$secretFile = Join-Path $scriptDir '.r2_endpoint'

if (-not (Test-Path $secretFile)) {
    throw "Private endpoint file not found: $secretFile`nCreate the file (containing the endpoint URL) and add it to .gitignore."
}

# Read first non-empty line, trim whitespace
$EndpointUrl = Get-Content $secretFile -ErrorAction Stop |
               ForEach-Object { $_.Trim() } |
               Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
               Select-Object -First 1

if (-not $EndpointUrl) {
    throw "Private endpoint file exists but contains no valid URL: $secretFile"
}

# Basic validation that it's a well-formed absolute URI
if (-not [Uri]::IsWellFormedUriString($EndpointUrl, [UriKind]::Absolute)) {
    throw "Endpoint in $secretFile does not look like a valid absolute URL: $EndpointUrl"
}

Write-Host "Using private endpoint from $secretFile"
# $EndpointUrl is now safe to use below

function Write-Log { param($msg) Write-Host "[*] $msg" }

try {
    # Resolve script directory and parent dir (where gradlew / gradle.properties live)
    $scriptPath = $MyInvocation.MyCommand.Path
    if (-not $scriptPath) { $scriptPath = (Get-Location).Path }   # fallback if run in interactive session
    $scriptDir = Split-Path -Parent $scriptPath
    $parentDir = Resolve-Path (Join-Path $scriptDir '..') | Select-Object -ExpandProperty Path

    Write-Log "Using parent directory: $parentDir"

    # Find gradlew
    $gradlewPath = Join-Path $parentDir 'gradlew.bat'

    # Run gradlew clean build
    Write-Log "Running 'gradlew clean build'..."
    Push-Location $parentDir
    try {
        # Use Start-Process for reliable exit code capture cross-platform
        $proc = Start-Process -FilePath $gradlewPath -ArgumentList 'clean','build' -NoNewWindow -Wait -PassThru
        if ($proc.ExitCode -ne 0) {
            throw "Gradle build failed with exit code $($proc.ExitCode)."
        }
    } finally {
        Pop-Location
    }

    Write-Log "Build finished successfully."

    # Read version from gradle.properties
    $gpPath = Join-Path $parentDir 'gradle.properties'
    if (-not (Test-Path $gpPath)) {
        throw "gradle.properties not found at $gpPath"
    }

    $gpLines = Get-Content $gpPath -ErrorAction Stop
    $versionLine = $gpLines | Select-String -Pattern '^\s*version\s*=' -AllMatches | ForEach-Object { $_.Line } | Select-Object -First 1

    if (-not $versionLine) {
        throw "Could not find a 'version=' line in gradle.properties."
    }

    # Extract the value after '=' and trim quotes/whitespace
    $version = $versionLine -replace '^\s*version\s*=\s*',''
    $version = $version.Trim()
    #$version = $version.Trim("'`" + '"' )
    if (-not $version) { throw "Parsed version is empty." }

    Write-Log "Parsed version: $version"

    # Find the latest JAR in build/libs
    $libsDir = Join-Path $parentDir 'build/libs'
    if (-not (Test-Path $libsDir)) { throw "Libraries folder not found at $libsDir" }

    $jar = Get-ChildItem -Path $libsDir -Filter '*.jar' -File -ErrorAction Stop |
           Sort-Object LastWriteTime -Descending |
           Select-Object -First 1

    if (-not $jar) { throw "No .jar files found in $libsDir" }

    $jarPath = $jar.FullName
    $jarName = $jar.Name

    Write-Log "Selected jar: $jarName"

    # Prepare S3 keys
    $prefix = "builds"
    $jarKey = "$prefix/$jarName"
    $latestJsonKey = "$prefix/latest.json"

    # Build JSON
    $latestObj = @{ version = $version }
    $latestJson = $latestObj | ConvertTo-Json -Compress

    Write-Log "Will upload jar to: r2://$BucketName/$jarKey"
    Write-Log "Will upload latest.json to: r2://$BucketName/$latestJsonKey"

    # Detect available uploader: aws CLI or AWS PowerShell cmdlets
    $awsCli = Get-Command aws -ErrorAction SilentlyContinue

    if ($awsCli) {
        Write-Log "Uploading using AWS CLI."

        # Upload jar
        $jarKeyPS = $jarKey
        $jarArgs = @('s3', 'cp', $jarPath, "s3://blockgame-assets/$jarKeyPS", "--endpoint-url $EndpointUrl")

        Write-Log "Writing jar $BucketName/$jarKeyPS"
        $proc = Start-Process -FilePath $awsCli.Source -ArgumentList $jarArgs -NoNewWindow -Wait -PassThru
        if ($proc.ExitCode -ne 0) { throw "aws s3 cp (jar) failed with exit code $($proc.ExitCode)." }

        # latest.json -> temp file, upload
        $tmpJson = [System.IO.Path]::GetTempFileName()
        Set-Content -Path $tmpJson -Value $latestJson -Encoding UTF8
        $jsonArgs  = @('s3', 'cp', $tmpJson, "s3://blockgame-assets/$latestJsonKey", "--endpoint-url $EndpointUrl")

        Write-Log "Writing latest.json $BucketName/$latestJsonKey"
        $proc = Start-Process -FilePath $awsCli.Source -ArgumentList $jsonArgs  -NoNewWindow -Wait -PassThru
        if ($proc.ExitCode -ne 0) { throw "aws s3 cp (version) failed with exit code $($proc.ExitCode)." }

        Remove-Item $tmpJson -ErrorAction SilentlyContinue
        Write-Log "Upload complete."
    }
    else {
        throw "No AWS CLI is available. Install one to upload to S3."
    }

    Write-Host
    Write-Log "Success: uploaded $jarName and latest.json (version $version) to https://assets.blockgame.james090500.com/$prefix"
    exit 0
}
catch {
    Write-Error "ERROR: $($_.Exception.Message)"
    exit 1
}