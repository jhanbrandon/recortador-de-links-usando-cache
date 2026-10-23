$ErrorActionPreference='Stop'
$tools=Join-Path $PSScriptRoot '..\.tools'
New-Item -ItemType Directory -Force -Path $tools | Out-Null
$archive=Join-Path $tools 'apache-maven-3.9.9-bin.zip'
$url='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip'
Invoke-WebRequest $url -OutFile $archive -UseBasicParsing
$expected=(Invoke-WebRequest ($url+'.sha512') -UseBasicParsing).Content.Trim().Split(' ')[0]
$actual=(Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash
if($actual -ne $expected) { throw 'La verificacion SHA512 de Maven fallo.' }
Expand-Archive -LiteralPath $archive -DestinationPath $tools -Force