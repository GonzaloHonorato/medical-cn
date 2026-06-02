#!/bin/sh
# Extrae el Oracle wallet desde la variable de entorno base64
if [ -n "$ORACLE_WALLET_BASE64" ]; then
  mkdir -p /app/wallet
  echo "$ORACLE_WALLET_BASE64" | base64 -d | tar xzf - -C /app/wallet
  echo "Wallet extraido en /app/wallet"
fi

exec java -jar /app/app.jar
