# CodeGym GitHub structure check. Run this script from any current directory.
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$required = @(
    'pom.xml',
    'README.md',
    'database/demo.sql',
    'database/stored_procedures.sql',
    'src/main/java/com/codegym/model/User.java',
    'src/main/java/com/codegym/dao/IUserDAO.java',
    'src/main/java/com/codegym/dao/UserDAO.java',
    'src/main/java/com/codegym/controller/UserServlet.java',
    'src/main/webapp/index.jsp',
    'src/main/webapp/user/list.jsp',
    'src/main/webapp/user/create.jsp',
    'src/main/webapp/user/edit.jsp',
    'src/main/webapp/user/delete.jsp',
    'src/main/webapp/WEB-INF/web.xml'
)
$missing = @($required | Where-Object { -not (Test-Path (Join-Path $root $_) -PathType Leaf) })
if ($missing.Count -gt 0) {
    Write-Host 'FAILED: Missing essential project files at repository root:' -ForegroundColor Red
    $missing | ForEach-Object { Write-Host "  - $_" }
    exit 1
}
$gitRoot = git -C $root rev-parse --show-toplevel 2>$null
if ($LASTEXITCODE -eq 0 -and $gitRoot) {
    $gitRoot = [IO.Path]::GetFullPath($gitRoot.Trim()).TrimEnd('\','/')
    $projectRoot = [IO.Path]::GetFullPath($root).TrimEnd('\','/')
    if ($gitRoot -ne $projectRoot) {
        Write-Host 'WARNING: Project files are NOT at Git repository root.' -ForegroundColor Yellow
        Write-Host "Git root: $gitRoot"
        Write-Host "Project:  $projectRoot"
        exit 2
    }
}
Write-Host "PASS: All important Maven/JSP/Servlet/JDBC files exist at root: $root" -ForegroundColor Green
Write-Host 'Before CodeGym submission, verify the same files on the GitHub repository MAIN PAGE.'
