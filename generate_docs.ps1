# Путь к выходному файлу
$OUTPUT_FILE = "docs/prompt.md"

# Очистка предыдущего содержимого файла
Set-Content -Path $OUTPUT_FILE -Value ""

# Функция для добавления содержимого файла в prompt.md
function Add-FileContent {
    param (
        [string]$file
    )
    if (Test-Path $file) {
        $relativePath = $file -replace [regex]::Escape((Get-Location).Path), ""
        Add-Content -Path $OUTPUT_FILE -Value "## $relativePath"
        Add-Content -Path $OUTPUT_FILE -Value '```'
        Get-Content -Path $file | Add-Content -Path $OUTPUT_FILE
        Add-Content -Path $OUTPUT_FILE -Value '```'
        Add-Content -Path $OUTPUT_FILE -Value ""
    }
}

# Функция для отображения структуры с табуляцией
function Get-Structure {
    param (
        [string]$path,
        [int]$indentLevel = 0
    )
    $items = Get-ChildItem -Path $path
    foreach ($item in $items) {
        $relativePathStructure = $item.FullName -replace [regex]::Escape((Get-Location).Path ), ""
        $indent = " " * ($indentLevel * 4) # 4 пробела на уровень вложенности
        if ($item.PSIsContainer) {
            Add-Content -Path $OUTPUT_FILE -Value "$indent\$($item.Name)"
            Get-Structure -path $item.FullName -indentLevel ($indentLevel + 1)
        } else {
            Add-Content -Path $OUTPUT_FILE -Value "$indent$($item.Name)"
        }
    }
}

# Добавление структуры проекта
Add-Content -Path $OUTPUT_FILE -Value "# Структура исходного кода проекта"
Add-Content -Path $OUTPUT_FILE -Value '```'
Get-Structure -path "src"
Add-Content -Path $OUTPUT_FILE -Value '```'

# Добавление содержимого файлов
Add-Content -Path $OUTPUT_FILE -Value "# Исходный код проекта"

# Добавление pom.xml
Add-FileContent "pom.xml"

# Добавление Dockerfile
Add-FileContent "Dockerfile"

# Добавление docker-compose.yml
Add-FileContent "docker-compose.yml"

# Добавление всех файлов из папки src
Get-ChildItem -Recurse -Path src -Include *.java, *.xml, *.html | ForEach-Object {
    Add-FileContent $_.FullName
}