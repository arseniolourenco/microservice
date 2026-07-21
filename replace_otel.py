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

brave_dep = re.compile(r"<dependency>\s*<groupId>io\.micrometer</groupId>\s*<artifactId>micrometer-tracing-bridge-brave</artifactId>\s*</dependency>")
zipkin_dep = re.compile(r"<dependency>\s*<groupId>io\.zipkin\.reporter2</groupId>\s*<artifactId>zipkin-reporter-brave</artifactId>\s*</dependency>")

otel_replacement = """<dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-otel</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-zipkin</artifactId>
        </dependency>"""

for file in files_to_check:
    if os.path.exists(file):
        with open(file, 'r') as f:
            content = f.read()
        
        if brave_dep.search(content) or zipkin_dep.search(content):
            print(f"Modifying {file}...")
            # We replace brave_dep with otel_replacement, and remove zipkin_dep
            content = brave_dep.sub(otel_replacement, content)
            content = zipkin_dep.sub("", content)
            
            with open(file, 'w') as f:
                f.write(content)
        else:
            print(f"Skipping {file} (dependencies not found)")
    else:
        print(f"File {file} not found")

