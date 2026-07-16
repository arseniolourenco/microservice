from ruamel.yaml import YAML

yaml = YAML()
yaml.preserve_quotes = True

with open('docker-compose.yml', 'r') as f:
    data = yaml.load(f)

# 1. Add healthchecks to infra services that miss them
if 'zookeeper' in data['services'] and 'healthcheck' not in data['services']['zookeeper']:
    data['services']['zookeeper']['healthcheck'] = {
        'test': ['CMD', 'echo', 'ruok', '|', 'nc', 'localhost', '2181', '||', 'exit', '1'],
        'interval': '10s',
        'timeout': '5s',
        'retries': 5
    }

if 'kafka' in data['services'] and 'healthcheck' not in data['services']['kafka']:
    data['services']['kafka']['healthcheck'] = {
        'test': ['CMD', 'kafka-topics', '--bootstrap-server', 'localhost:9092', '--list'],
        'interval': '30s',
        'timeout': '10s',
        'retries': 5,
        'start_period': '30s'
    }

if 'keycloak' in data['services'] and 'healthcheck' not in data['services']['keycloak']:
    data['services']['keycloak']['healthcheck'] = {
        'test': ['CMD-SHELL', 'exec 3<>/dev/tcp/localhost/8080 && echo -e "GET /health/ready HTTP/1.1\r\nhost: localhost:8080\r\nConnection: close\r\n\r\n" >&3 && grep "200 OK" <&3'],
        'interval': '30s',
        'timeout': '10s',
        'retries': 5,
        'start_period': '30s'
    }

# 2. Add config-server dependency to all microservices & convert service_started to service_healthy
microservices = ['api-gateway', 'discovery-server', 'order-service', 'product-service', 'inventory-service', 'notification-service', 'user-service']

for s_name, service in data['services'].items():
    if 'depends_on' in service:
        deps = service['depends_on']
        if isinstance(deps, dict):
            for dep_name in list(deps.keys()):
                # If the target dependency has a healthcheck, we can use service_healthy
                target_service = data['services'].get(dep_name, {})
                if 'healthcheck' in target_service:
                    deps[dep_name]['condition'] = 'service_healthy'
                else:
                    deps[dep_name]['condition'] = 'service_started'
        elif isinstance(deps, list):
            # Convert list to dict
            new_deps = {}
            for dep in deps:
                target_service = data['services'].get(dep, {})
                if 'healthcheck' in target_service:
                    new_deps[dep] = {'condition': 'service_healthy'}
                else:
                    new_deps[dep] = {'condition': 'service_started'}
            service['depends_on'] = new_deps
            deps = service['depends_on']

    # For microservices, ensure config-server dependency
    if s_name in microservices:
        if 'depends_on' not in service:
            service['depends_on'] = {}
        # discovery-server and config-server do not depend on config-server (config-server doesn't, discovery-server does)
        if s_name != 'config-server':
            if 'config-server' not in service['depends_on']:
                service['depends_on']['config-server'] = {'condition': 'service_healthy'}
                
            # Convert config-server to healthy if not already
            if service['depends_on']['config-server'].get('condition') != 'service_healthy':
                service['depends_on']['config-server']['condition'] = 'service_healthy'

with open('docker-compose.yml', 'w') as f:
    yaml.dump(data, f)
