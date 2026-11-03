@echo off
setlocal
set DIR=%~dp0
set GRADLE_OPTS=%GRADLE_OPTS% -Dorg.gradle.project.android.builder.sdkDownload=true
"%DIR%gradle\wrapper\gradle-wrapper.jar" %*
