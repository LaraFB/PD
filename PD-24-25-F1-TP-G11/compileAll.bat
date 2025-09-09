@echo A compilar o projeto ...

@echo off
setlocal enabledelayedexpansion

set "source_files="
for /r %%f in (*.java) do (
    set "source_files=!source_files! "%%f""
)

if not exist .\bin mkdir .\bin

javac --module-path "C:\Program Files\Java\javafx-sdk-21.0.5\lib" --add-modules javafx.controls,javafx.fxml -d .\bin !source_files!

@echo on
@echo Compilacao terminada

pause