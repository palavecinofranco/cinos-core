#!/bin/bash

# Verificamos que esté la variable de entorno
if [ -z "$GCP_CREDENTIALS_BASE64" ]; then
  echo "⚠️  Variable GCP_CREDENTIALS_BASE64 no está definida."
  exit 1
fi

# Crear archivo de credenciales desde el base64
echo "$GCP_CREDENTIALS_BASE64" | base64 -d > "$GOOGLE_APPLICATION_CREDENTIALS"

# Ejecutar la app
exec java -jar app.jar
