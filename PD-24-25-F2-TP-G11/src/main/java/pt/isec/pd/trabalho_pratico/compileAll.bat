@echo off
setlocal enabledelayedexpansion

set "base_dados_dir=BaseDados"
set "rmi_dir=rmi"
set "source_files="

for /r "%base_dados_dir%" %%f in (*.java) do (
        set "source_files=!source_files! "%%f""
)

for /r "%rmi_dir%" %%f in (*.java) do (
    if /i not "%%~nxf"=="ServerRMI.java" (
        set "source_files=!source_files! "%%f""
    )
)
if not exist .\bin mkdir .\bin
javac -d .\bin !source_files!

@echo on