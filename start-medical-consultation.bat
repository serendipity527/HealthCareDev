@echo off
chcp 65001 > nul
echo.
echo ========================================
echo   医疗咨询状态图系统启动脚本
echo ========================================
echo.

:: 检查Java环境
echo [1/4] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到Java环境，请先安装Java 17或更高版本
    pause
    exit /b 1
)
echo ✓ Java环境检查通过
echo.

:: 检查Maven环境
echo [2/4] 检查Maven环境...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到Maven环境，请先安装Maven
    pause
    exit /b 1
)
echo ✓ Maven环境检查通过
echo.

:: 编译项目
echo [3/4] 编译项目...
echo 正在执行 mvn clean install...
call mvn clean install -DskipTests
if errorlevel 1 (
    echo ❌ 编译失败，请检查错误信息
    pause
    exit /b 1
)
echo ✓ 项目编译成功
echo.

:: 启动应用
echo [4/4] 启动应用...
echo.
echo ========================================
echo   应用正在启动...
echo ========================================
echo.
echo 📌 访问地址：
echo    - 医疗咨询页面: http://localhost:8080/medical-consultation.html
echo    - 简单聊天页面: http://localhost:8080/chat.html
echo    - API接口: http://localhost:8080/api/consultation/ask
echo.
echo 💡 提示：
echo    - 按 Ctrl+C 停止服务器
echo    - 查看日志输出了解运行状态
echo.
echo ========================================
echo.

call mvn spring-boot:run

pause

