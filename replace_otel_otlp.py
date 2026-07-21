import os
import re

files_to_check = [
    "api-gateway/pom.xml",
    "auth-service/pom.xml",
    "config-server/pom.xml",
    "discovery-server/pom.xml",
    "inventory-service/pom.xml",
    "notification-service/pom.xml",
    "order-service/pom.xml",
    "product-service/pom.xml",
    "user-service/pom.xml"
]

for file in files_to_check:
    if os.path.exists(file):
        with open(file, 'r') as f:
            content = f.read()
        
        if "opentelemetry-exporter-zipkin" in content:
            print(f"Modifying {file}...")
            content = content.replace("opentelemetry-exporter-zipkin", "opentelemetry-exporter-otlp")
            
            with open(file, 'w') as f:
                f.write(content)
        else:
            print(f"Skipping {file} (dependencies not found)")
    else:
        print(f"File {file} not found")

