@echo off
chcp 65001 > nul
echo.
echo ========================================
echo   医疗咨询API测试脚本
echo ========================================
echo.

:: 检查curl是否可用
curl --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到curl工具
    echo 请安装curl或使用浏览器测试
    pause
    exit /b 1
)

:menu
echo.
echo 请选择测试场景：
echo.
echo 1. 高危医疗情况（胸痛+呼吸困难）
echo 2. 一般医疗咨询（头疼）
echo 3. 通用聊天（问候）
echo 4. 自定义消息
echo 5. 健康检查
echo 0. 退出
echo.
set /p choice="请输入选项（0-5）: "

if "%choice%"=="1" goto test_emergency
if "%choice%"=="2" goto test_medical
if "%choice%"=="3" goto test_general
if "%choice%"=="4" goto test_custom
if "%choice%"=="5" goto test_health
if "%choice%"=="0" goto end
echo 无效选项，请重新选择
goto menu

:test_emergency
echo.
echo 测试场景1：高危医疗情况
echo ----------------------------------------
echo 发送消息: "我突然感到剧烈胸痛，无法呼吸"
echo.
curl -X POST http://localhost:8080/api/consultation/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"test_user_001\",\"message\":\"我突然感到剧烈胸痛，无法呼吸\"}"
echo.
echo ----------------------------------------
goto menu

:test_medical
echo.
echo 测试场景2：一般医疗咨询
echo ----------------------------------------
echo 发送消息: "我最近总是头疼，怎么办？"
echo.
curl -X POST http://localhost:8080/api/consultation/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"test_user_002\",\"message\":\"我最近总是头疼，怎么办？\"}"
echo.
echo ----------------------------------------
goto menu

:test_general
echo.
echo 测试场景3：通用聊天
echo ----------------------------------------
echo 发送消息: "你好，今天天气真好"
echo.
curl -X POST http://localhost:8080/api/consultation/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"test_user_003\",\"message\":\"你好，今天天气真好\"}"
echo.
echo ----------------------------------------
goto menu

:test_custom
echo.
set /p custom_msg="请输入自定义消息: "
echo.
echo 测试场景4：自定义消息
echo ----------------------------------------
echo 发送消息: "%custom_msg%"
echo.
curl -X POST http://localhost:8080/api/consultation/ask ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"test_user_custom\",\"message\":\"%custom_msg%\"}"
echo.
echo ----------------------------------------
goto menu

:test_health
echo.
echo 测试场景5：健康检查
echo ----------------------------------------
curl -X GET http://localhost:8080/api/consultation/health
echo.
echo ----------------------------------------
goto menu

:end
echo.
echo 测试结束
echo.
pause

