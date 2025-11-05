@echo off
chcp 65001 >nul
echo ========================================
echo    WebSocket 聊天室启动脚本
echo ========================================
echo.
echo [1] 正在启动 Spring Boot 应用...
echo.

call mvn spring-boot:run

echo.
echo ========================================
echo 应用已停止
echo ========================================
pause

