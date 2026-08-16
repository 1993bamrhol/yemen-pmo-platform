$directories = @(
    "backend",
    "frontend",
    "database",
    "infrastructure",
    "docs",
    "docs/00_Project_Charter",
    "docs/01_Feasibility",
    "docs/02_BRD",
    "docs/03_SRS",
    "docs/04_Architecture",
    "docs/05_Database",
    "docs/06_API",
    "docs/07_Project_Management",
    "docs/08_Operations",
    "docs/09_User_Documentation",
    "scripts",
    "tests",
    ".github"
)

$files = @(
    "README.md",
    "LICENSE",
    "CHANGELOG.md",
    "CONTRIBUTING.md",
    "SECURITY.md",
    ".gitignore"
)

Write-Host ""
Write-Host "Creating project structure..."
Write-Host ""

foreach ($dir in $directories) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
        Write-Host "[OK] Directory: $dir"
    }
}

foreach ($file in $files) {
    if (!(Test-Path $file)) {
        New-Item -ItemType File -Path $file | Out-Null
        Write-Host "[OK] File: $file"
    }
}

Write-Host ""
Write-Host "========================================="
Write-Host " Yemen PMO Platform structure created."
Write-Host "========================================="