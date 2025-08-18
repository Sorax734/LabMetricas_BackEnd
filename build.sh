#!/bin/bash

# Script de build para Render
echo "🚀 Iniciando build de LabMetricas Backend..."

# Verificar que Maven esté disponible
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven no está instalado. Usando Maven wrapper..."
    chmod +x ./mvnw
    ./mvnw clean package -DskipTests
else
    echo "✅ Maven encontrado. Ejecutando build..."
    mvn clean package -DskipTests
fi

# Verificar que el JAR se haya creado
if [ -f "target/LabMetricas-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ Build completado exitosamente!"
    echo "📦 JAR creado: target/LabMetricas-0.0.1-SNAPSHOT.jar"
    ls -la target/LabMetricas-0.0.1-SNAPSHOT.jar
else
    echo "❌ Error: No se pudo crear el JAR"
    exit 1
fi
