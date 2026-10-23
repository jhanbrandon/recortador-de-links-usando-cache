param([ValidateSet('dev','build','preview')][string]$Task='dev')
$ErrorActionPreference='Stop'
# Google Drive virtual disks can reject npm file writes; sources stay in the workspace.
$local=Join-Path $env:TEMP 'parcial2-url-shortener/frontend'
New-Item -ItemType Directory -Force -Path $local | Out-Null
foreach($file in @('package.json','package-lock.json','index.html','vite.config.js','smoke.mjs','.env')) {
 $source=Join-Path $PSScriptRoot ('frontend/'+$file)
 if(Test-Path -LiteralPath $source) { Copy-Item -LiteralPath $source -Destination $local -Force }
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'frontend/src') -Destination $local -Recurse -Force
Push-Location $local
try {
 $lockHash=(Get-FileHash -LiteralPath 'package-lock.json' -Algorithm SHA256).Hash
 $marker=Join-Path $local '.dependencies.sha256'
 if(!(Test-Path 'node_modules/vite/bin/vite.js') -or !(Test-Path $marker) -or ([IO.File]::ReadAllText($marker) -ne $lockHash)) {
  npm.cmd ci
  if($LASTEXITCODE -ne 0) { throw 'No fue posible instalar las dependencias. Deten Vite antes de reinstalarlas.' }
  [IO.File]::WriteAllText($marker,$lockHash)
 }
 Write-Host "Frontend local: $local"
 npm.cmd run $Task
 if($LASTEXITCODE -ne 0) { throw 'Vite termino con un error.' }
} finally { Pop-Location }