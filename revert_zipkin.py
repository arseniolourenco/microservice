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

otel_dep = re.compile(r"<dependency>\s*<groupId>io\.micrometer</groupId>\s*<artifactId>micrometer-tracing-bridge-otel</artifactId>\s*</dependency>\s*<dependency>\s*<groupId>io\.opentelemetry</groupId>\s*<artifactId>opentelemetry-exporter-otlp</artifactId>\s*</dependency>")

brave_replacement = """<dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>"""

for file in files_to_check:
    if os.path.exists(file):
        with open(file, 'r') as f:
            content = f.read()
        
        if otel_dep.search(content):
            print(f"Modifying {file}...")
            content = otel_dep.sub(brave_replacement, content)
            
            with open(file, 'w') as f:
                f.write(content)
        else:
            print(f"Skipping {file} (dependencies not found)")
    else:
        print(f"File {file} not found")

