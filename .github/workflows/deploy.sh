#!/bin/bash
set -e

echo "===== 1. 构建后端 ====="
sudo mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "后端构建失败，退出"
    exit 1
fi

echo ""
echo "===== 2. 构建前端 ====="
sudo npm install && sudo npm run build
if [ $? -ne 0 ]; then
    echo "前端构建失败，退出"
    exit 1
fi

echo ""
echo "===== 3. 停止后端服务 ====="
sudo systemctl stop j-lib.service

echo ""
echo "===== 4. 部署后端 jar ====="
sudo cp target/springboot-vue3-system-1.0.0.jar /home/springboot-vue3-system-1.0.0.jar

echo ""
echo "===== 5. 启动后端服务 ====="
sudo systemctl daemon-reload
sudo systemctl start j-lib.service

echo ""
echo "===== 6. 部署前端静态文件 ====="
sudo rm -rf /home/g/*
sudo cp -r dist/* /home/g

echo ""
echo "===== 7. 重启 Nginx ====="
sudo systemctl restart nginx

echo ""
echo "===== 部署完成 ====="
echo "后端: http://localhost:8081"
echo "前端: http://localhost:80"
