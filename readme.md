# Istio Service Mesh Demo on Azure AKS

A comprehensive guide to deploy two Spring Boot microservices on Azure Kubernetes Service with Istio service mesh, demonstrating advanced traffic management, security, and observability features.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Part 1: Setup Project Files](#part-1-setup-project-files)
- [Part 2: Azure Infrastructure Setup](#part-2-azure-infrastructure-setup)
- [Part 3: Build and Push Docker Images](#part-3-build-and-push-docker-images)
- [Part 4: Install Istio](#part-4-install-istio)
- [Part 5: Deploy Applications](#part-5-deploy-applications)
- [Part 6: Verify Deployment](#part-6-verify-deployment)
- [Part 7: Istio Traffic Management Scenarios](#part-7-istio-traffic-management-scenarios)
- [Part 8: Observability](#part-8-observability)
- [Part 9: Security Features](#part-9-security-features)
- [Troubleshooting](#troubleshooting)
- [Cleanup](#cleanup)
- [Cost Estimation](#cost-estimation)

---

## 🎯 Overview

This project demonstrates:

- **Two microservices**: Order Service (calls Product Service) and Product Service (uses PostgreSQL)
- **Istio service mesh**: Complete setup with all observability tools
- **Traffic management**: Canary, blue-green, A/B testing, mirroring, fault injection, circuit breakers
- **Security**: Mutual TLS (mTLS), authorization policies
- **Observability**: Kiali, Grafana, Jaeger, Prometheus

---

## 🏗️ Architecture

```
Internet
    ↓
Azure Load Balancer
    ↓
Istio Ingress Gateway
    ↓
Order Service (v1, v2) ─────→ Product Service (v1, v2)
                                      ↓
                              Azure PostgreSQL
```

**Key Features:**
- All traffic encrypted with mTLS
- All requests traced and monitored
- Traffic routing controlled by Istio
- Circuit breakers and resilience patterns
- Real-time metrics and visualization

---

## ✅ Prerequisites

### Required Tools
- **Azure CLI** (2.50+): [Install](https://docs.microsoft.com/cli/azure/install-azure-cli)
- **kubectl** (1.28+): [Install](https://kubernetes.io/docs/tasks/tools/)
- **Maven** (3.8+): [Install](https://maven.apache.org/install.html)
- **Java JDK** (17+): [Install](https://adoptium.net/)
- **Docker** (24+): [Install](https://docs.docker.com/get-docker/)
- **curl** and **jq**: For testing

### Azure Requirements
- Active Azure subscription
- Sufficient quota for:
    - 3 VMs (Standard_D2s_v3)
    - 1 Load Balancer
    - 1 Public IP
    - 1 PostgreSQL server

### Local Setup
```bash
# Verify installations
az --version
kubectl version --client
mvn --version
java --version
docker --version
curl --version
jq --version
```

---

## 📁 Project Structure

Create this directory structure:

```
istio-demo/
├── order-service/
│   ├── src/main/java/com/example/order/
│   │   ├── OrderServiceApplication.java
│   │   ├── controller/OrderController.java
│   │   └── service/ProductClient.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
├── product-service/
│   ├── src/main/java/com/example/product/
│   │   ├── ProductServiceApplication.java
│   │   ├── entity/Product.java
│   │   ├── repository/ProductRepository.java
│   │   ├── controller/ProductController.java
│   │   └── config/DataInitializer.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
├── k8s-manifests/
│   ├── namespace.yaml
│   ├── postgres-secret.yaml
│   ├── product-service-v1.yaml
│   ├── product-service-v2.yaml
│   ├── product-service-svc.yaml
│   ├── order-service-v1.yaml
│   ├── order-service-v2.yaml
│   └── order-service-svc.yaml
├── istio-configs/
│   ├── gateway.yaml
│   ├── virtualservice-order.yaml
│   ├── virtualservice-product.yaml
│   ├── destinationrule-order.yaml
│   ├── destinationrule-product.yaml
│   ├── peer-authentication.yaml
│   ├── authorization-policy.yaml
│   └── authorization-policy-ingress.yaml
├── istio-scenarios/
│   ├── canary-order.yaml
│   ├── bluegreen-order.yaml
│   ├── ab-testing-order.yaml
│   ├── mirror-order.yaml
│   ├── fault-injection-product.yaml
│   ├── circuit-breaker-product.yaml
│   └── timeout-retry-order.yaml
└── README.md (this file)
```

---

## 🚀 Part 1: Setup Project Files

### Step 1.1: Create Directory Structure

```bash
mkdir -p istio-demo
cd istio-demo

# Create Java project directories
mkdir -p order-service/src/main/java/com/example/order/{controller,service}
mkdir -p order-service/src/main/resources
mkdir -p product-service/src/main/java/com/example/product/{entity,repository,controller,config}
mkdir -p product-service/src/main/resources

# Create Kubernetes and Istio directories
mkdir -p k8s-manifests
mkdir -p istio-configs
mkdir -p istio-scenarios
```

### Step 1.2: Copy All Source Files

Copy all the Java source code, Kubernetes manifests, and Istio configurations from the "Complete Istio Demo Guide - All-in-One" artifact into their respective directories.

**Files to create:**

**Order Service (9 files):**
- `order-service/pom.xml`
- `order-service/Dockerfile`
- `order-service/src/main/java/com/example/order/OrderServiceApplication.java`
- `order-service/src/main/java/com/example/order/controller/OrderController.java`
- `order-service/src/main/java/com/example/order/service/ProductClient.java`
- `order-service/src/main/resources/application.yml`

**Product Service (11 files):**
- `product-service/pom.xml`
- `product-service/Dockerfile`
- `product-service/src/main/java/com/example/product/ProductServiceApplication.java`
- `product-service/src/main/java/com/example/product/entity/Product.java`
- `product-service/src/main/java/com/example/product/repository/ProductRepository.java`
- `product-service/src/main/java/com/example/product/controller/ProductController.java`
- `product-service/src/main/java/com/example/product/config/DataInitializer.java`
- `product-service/src/main/resources/application.yml`

**Kubernetes Manifests (8 files):**
- All files in `k8s-manifests/` directory

**Istio Configurations (8 files):**
- All files in `istio-configs/` directory

**Istio Scenarios (7 files):**
- All files in `istio-scenarios/` directory

---

## ☁️ Part 2: Azure Infrastructure Setup

### Step 2.1: Set Environment Variables

```bash
export RESOURCE_GROUP="rg-istio-demo"
export LOCATION="eastus"
export AKS_CLUSTER_NAME="aks-istio-cluster"
export ACR_NAME="acristio$(date +%s)"
export POSTGRES_SERVER="postgres-istio-$(date +%s)"
export POSTGRES_ADMIN="adminuser"
export POSTGRES_PASSWORD="SecurePassword123!"
```

**⚠️ Important:** Change `POSTGRES_PASSWORD` to a strong password!

### Step 2.2: Login to Azure

```bash
az login

# Optional: Set specific subscription if you have multiple
az account list --output table
az account set --subscription "YOUR_SUBSCRIPTION_ID"
```

### Step 2.3: Create Resource Group

```bash
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

**Expected output:**
```json
{
  "id": "/subscriptions/.../resourceGroups/rg-istio-demo",
  "location": "eastus",
  "name": "rg-istio-demo",
  "properties": {
    "provisioningState": "Succeeded"
  }
}
```

### Step 2.4: Create Azure Container Registry

```bash
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --location $LOCATION
```

**Time:** ~2 minutes

### Step 2.5: Create PostgreSQL Server (Standard Tier)

```bash
az postgres server create \
  --resource-group $RESOURCE_GROUP \
  --name $POSTGRES_SERVER \
  --location $LOCATION \
  --admin-user $POSTGRES_ADMIN \
  --admin-password $POSTGRES_PASSWORD \
  --sku-name B_Gen5_1 \
  --version 11 \
  --storage-size 51200 \
  --ssl-enforcement Disabled
```

**Time:** ~5 minutes

**Note:** SSL is disabled for simplicity. Enable for production!

### Step 2.6: Create PostgreSQL Database

```bash
az postgres db create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --name products
```

### Step 2.7: Configure PostgreSQL Firewall

```bash
# Allow Azure services
az postgres server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# Allow all IPs (for testing only - restrict in production)
az postgres server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --name AllowAll \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255
```

### Step 2.8: Create AKS Cluster

```bash
az aks create \
  --resource-group $RESOURCE_GROUP \
  --name $AKS_CLUSTER_NAME \
  --node-count 3 \
  --node-vm-size Standard_D2s_v3 \
  --enable-managed-identity \
  --generate-ssh-keys \
  --network-plugin azure \
  --network-policy azure \
  --attach-acr $ACR_NAME \
  --enable-addons monitoring
```

**Time:** ~10-15 minutes ⏳

**What this creates:**
- 3 worker nodes (Standard_D2s_v3)
- Azure CNI networking
- Integration with ACR
- Azure Monitor enabled

### Step 2.9: Get AKS Credentials

```bash
az aks get-credentials \
  --resource-group $RESOURCE_GROUP \
  --name $AKS_CLUSTER_NAME \
  --overwrite-existing
```

### Step 2.10: Verify Cluster

```bash
kubectl cluster-info
kubectl get nodes
```

**Expected output:**
```
NAME                                STATUS   ROLES   AGE   VERSION
aks-nodepool1-12345678-vmss000000   Ready    agent   5m    v1.28.x
aks-nodepool1-12345678-vmss000001   Ready    agent   5m    v1.28.x
aks-nodepool1-12345678-vmss000002   Ready    agent   5m    v1.28.x
```

### Step 2.11: Save Configuration

```bash
# Save for later use
cat > azure-resources.env <<EOF
export RESOURCE_GROUP="$RESOURCE_GROUP"
export AKS_CLUSTER_NAME="$AKS_CLUSTER_NAME"
export ACR_NAME="$ACR_NAME"
export ACR_LOGIN_SERVER="${ACR_NAME}.azurecr.io"
export POSTGRES_SERVER="${POSTGRES_SERVER}.postgres.database.azure.com"
export POSTGRES_ADMIN="${POSTGRES_ADMIN}@${POSTGRES_SERVER}"
export POSTGRES_PASSWORD="$POSTGRES_PASSWORD"
EOF

# Load when needed
source azure-resources.env
```

**✅ Infrastructure setup complete!**

---

## 🐳 Part 3: Build and Push Docker Images

### Step 3.1: Build Java Applications

```bash
# Build Order Service
cd order-service
mvn clean package -DskipTests
cd ..

# Build Product Service
cd product-service
mvn clean package -DskipTests
cd ..
```

**Expected:** JAR files in `target/` directories

### Step 3.2: Login to ACR

```bash
source azure-resources.env
az acr login --name $ACR_NAME
```

### Step 3.3: Build and Push Images (Option A - Local Docker)

```bash
# Build and push Order Service
cd order-service
docker build -t ${ACR_LOGIN_SERVER}/order-service:v1 .
docker tag ${ACR_LOGIN_SERVER}/order-service:v1 ${ACR_LOGIN_SERVER}/order-service:v2
docker push ${ACR_LOGIN_SERVER}/order-service:v1
docker push ${ACR_LOGIN_SERVER}/order-service:v2
cd ..

# Build and push Product Service
cd product-service
docker build -t ${ACR_LOGIN_SERVER}/product-service:v1 .
docker tag ${ACR_LOGIN_SERVER}/product-service:v1 ${ACR_LOGIN_SERVER}/product-service:v2
docker push ${ACR_LOGIN_SERVER}/product-service:v1
docker push ${ACR_LOGIN_SERVER}/product-service:v2
cd ..
```

### Step 3.3: Build in Azure (Option B - No Local Docker)

```bash
# Build Order Service in ACR
az acr build \
  --registry $ACR_NAME \
  --image order-service:v1 \
  --file order-service/Dockerfile \
  ./order-service

az acr build \
  --registry $ACR_NAME \
  --image order-service:v2 \
  --file order-service/Dockerfile \
  ./order-service

# Build Product Service in ACR
az acr build \
  --registry $ACR_NAME \
  --image product-service:v1 \
  --file product-service/Dockerfile \
  ./product-service

az acr build \
  --registry $ACR_NAME \
  --image product-service:v2 \
  --file product-service/Dockerfile \
  ./product-service
```

### Step 3.4: Verify Images

```bash
az acr repository list --name $ACR_NAME --output table

# Should show:
# Result
# ------------------
# order-service
# product-service

az acr repository show-tags --name $ACR_NAME --repository order-service --output table
az acr repository show-tags --name $ACR_NAME --repository product-service --output table

# Each should show: v1, v2
```

**✅ Docker images ready!**

---

## 🕸️ Part 4: Install Istio

### Step 4.1: Download Istio

```bash
cd istio-demo
curl -L https://istio.io/downloadIstio | sh -

# Navigate to Istio directory (adjust version as needed)
cd istio-1.*
export PATH=$PWD/bin:$PATH

# Verify installation
istioctl version
```

### Step 4.2: Install Istio with Demo Profile

```bash
istioctl install --set profile=demo -y
```

**Time:** ~2 minutes

**What gets installed:**
- Istio control plane (istiod)
- Istio ingress gateway
- Istio egress gateway

### Step 4.3: Wait for Istio Pods

```bash
kubectl wait --for=condition=ready pod --all -n istio-system --timeout=300s

# Verify
kubectl get pods -n istio-system
```

**Expected output:**
```
NAME                                    READY   STATUS    RESTARTS   AGE
istio-egressgateway-xxx                 1/1     Running   0          2m
istio-ingressgateway-xxx                1/1     Running   0          2m
istiod-xxx                              1/1     Running   0          2m
```

### Step 4.4: Install Observability Addons

```bash
# Prometheus (metrics collection)
kubectl apply -f samples/addons/prometheus.yaml

# Grafana (metrics visualization)
kubectl apply -f samples/addons/grafana.yaml

# Kiali (service mesh visualization)
kubectl apply -f samples/addons/kiali.yaml

# Jaeger (distributed tracing)
kubectl apply -f samples/addons/jaeger.yaml
```

### Step 4.5: Wait for Addon Pods

```bash
kubectl wait --for=condition=ready pod --all -n istio-system --timeout=300s

# Verify all pods
kubectl get pods -n istio-system
```

**Expected:** All pods in Running state

### Step 4.6: Get Ingress Gateway IP

```bash
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')

echo "Istio Ingress Gateway IP: $INGRESS_HOST"
echo "Ingress Port: $INGRESS_PORT"
echo "Gateway URL: http://${INGRESS_HOST}:${INGRESS_PORT}"
```

**⚠️ Note:** It may take 2-3 minutes for the external IP to be assigned.

If IP shows as `<pending>`, wait and run again:
```bash
kubectl get svc istio-ingressgateway -n istio-system --watch
```

### Step 4.7: Return to Project Root

```bash
cd ..
```

**✅ Istio installation complete!**

---

## 🚢 Part 5: Deploy Applications

### Step 5.1: Update Kubernetes Manifests

```bash
source azure-resources.env

# Update image references
find k8s-manifests -type f -name "*.yaml" -exec sed -i.bak "s|YOUR_ACR.azurecr.io|${ACR_LOGIN_SERVER}|g" {} \;

# Update PostgreSQL secret (Linux/Mac)
sed -i.bak "s|YOUR_POSTGRES_SERVER.postgres.database.azure.com|${POSTGRES_SERVER}|g" k8s-manifests/postgres-secret.yaml
sed -i.bak "s|adminuser@YOUR_POSTGRES_SERVER|${POSTGRES_ADMIN}|g" k8s-manifests/postgres-secret.yaml
sed -i.bak "s|YourPassword123!|${POSTGRES_PASSWORD}|g" k8s-manifests/postgres-secret.yaml

# Clean up backup files
rm k8s-manifests/*.bak
```

**For Windows (Git Bash):**
```bash
find k8s-manifests -type f -name "*.yaml" -exec sed -i "s|YOUR_ACR.azurecr.io|${ACR_LOGIN_SERVER}|g" {} \;
sed -i "s|YOUR_POSTGRES_SERVER.postgres.database.azure.com|${POSTGRES_SERVER}|g" k8s-manifests/postgres-secret.yaml
sed -i "s|adminuser@YOUR_POSTGRES_SERVER|${POSTGRES_ADMIN}|g" k8s-manifests/postgres-secret.yaml
sed -i "s|YourPassword123!|${POSTGRES_PASSWORD}|g" k8s-manifests/postgres-secret.yaml
```

### Step 5.2: Create Namespace with Istio Injection

```bash
kubectl apply -f k8s-manifests/namespace.yaml

# Verify Istio injection label
kubectl get namespace microservices --show-labels
```

**Expected label:** `istio-injection=enabled`

### Step 5.3: Deploy Secrets

```bash
kubectl apply -f k8s-manifests/postgres-secret.yaml

# Verify secret
kubectl get secret postgres-credentials -n microservices
```

### Step 5.4: Deploy Product Service

```bash
# Deploy v1 and v2 deployments
kubectl apply -f k8s-manifests/product-service-v1.yaml
kubectl apply -f k8s-manifests/product-service-v2.yaml

# Deploy service
kubectl apply -f k8s-manifests/product-service-svc.yaml
```

### Step 5.5: Deploy Order Service

```bash
# Deploy v1 and v2 deployments
kubectl apply -f k8s-manifests/order-service-v1.yaml
kubectl apply -f k8s-manifests/order-service-v2.yaml

# Deploy service
kubectl apply -f k8s-manifests/order-service-svc.yaml
```

### Step 5.6: Wait for Pods to be Ready

```bash
# This may take 2-3 minutes for first deployment
kubectl wait --for=condition=ready pod -l app=product-service -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=order-service -n microservices --timeout=300s
```

### Step 5.7: Deploy Istio Configurations

```bash
# Deploy Gateway
kubectl apply -f istio-configs/gateway.yaml

# Deploy VirtualServices
kubectl apply -f istio-configs/virtualservice-order.yaml
kubectl apply -f istio-configs/virtualservice-product.yaml

# Deploy DestinationRules
kubectl apply -f istio-configs/destinationrule-order.yaml
kubectl apply -f istio-configs/destinationrule-product.yaml

# Deploy Security Policies
kubectl apply -f istio-configs/peer-authentication.yaml
kubectl apply -f istio-configs/authorization-policy.yaml
kubectl apply -f istio-configs/authorization-policy-ingress.yaml
```

### Step 5.8: Verify Istio Resources

```bash
kubectl get gateway,virtualservice,destinationrule -n microservices
```

**Expected output:**
```
NAME                                              AGE
gateway.networking.istio.io/microservices-gateway   1m

NAME                                                  GATEWAYS                    HOSTS   AGE
virtualservice.networking.istio.io/order-service     [microservices-gateway]     [*]     1m
virtualservice.networking.istio.io/product-service                               [product-service]     1m

NAME                                                      HOST              AGE
destinationrule.networking.istio.io/order-service         order-service     1m
destinationrule.networking.istio.io/product-service       product-service   1m
```

**✅ Applications deployed!**

---

## ✔️ Part 6: Verify Deployment

### Step 6.1: Check Pods Status

```bash
kubectl get pods -n microservices
```

**Expected output:**
```
NAME                                  READY   STATUS    RESTARTS   AGE
order-service-v1-xxx                  2/2     Running   0          5m
order-service-v2-xxx                  2/2     Running   0          5m
product-service-v1-xxx                2/2     Running   0          5m
product-service-v2-xxx                2/2     Running   0          5m
```

**Important:** `2/2` means application container + Istio sidecar proxy

### Step 6.2: Verify Istio Sidecar Injection

```bash
# Check containers in a pod
kubectl get pod -n microservices -l app=order-service -o jsonpath='{.items[0].spec.containers[*].name}'
```

**Expected output:** `order-service istio-proxy`

### Step 6.3: Check Services

```bash
kubectl get svc -n microservices
```

**Expected output:**
```
NAME              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
order-service     ClusterIP   10.0.123.45     <none>        8080/TCP   5m
product-service   ClusterIP   10.0.123.46     <none>        8080/TCP   5m
```

### Step 6.4: Test Application Endpoints

```bash
source azure-resources.env

# Test health endpoint
curl http://${INGRESS_HOST}/api/orders/health

# Expected: {"status":"UP","version":"v1"}
```

```bash
# Test main endpoint
curl http://${INGRESS_HOST}/api/orders/test

# Expected JSON response with service info
```

```bash
# Create an order
curl -X POST http://${INGRESS_HOST}/api/orders/create \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":2}'

# Expected: Order details with product information
```

### Step 6.5: Verify Database Connection

```bash
# Check product service logs
kubectl logs -n microservices -l app=product-service,version=v1 -c product-service --tail=50

# Look for: "Sample products initialized!"
```

### Step 6.6: Test Internal Service Communication

```bash
# Run a test pod
kubectl run test --image=curlimages/curl -it --rm -n microservices -- sh

# Inside the pod:
curl http://product-service:8080/api/products/PROD-001
exit
```

**✅ Deployment verified!**

---

## 🎛️ Part 7: Istio Traffic Management Scenarios

### Scenario 1: Canary Deployment (90% v1, 10% v2)

**Purpose:** Gradually roll out v2 to a small percentage of users.

```bash
# Apply canary configuration
kubectl apply -f istio-scenarios/canary-order.yaml

# Wait for configuration to propagate
sleep 5

# Test version distribution
for i in {1..20}; do
  curl -s http://${INGRESS_HOST}/api/orders/test | jq -r '.version'
done | sort | uniq -c
```

**Expected output:**
```
  18 v1
   2 v2
```

**Explanation:** 90% of traffic goes to v1, 10% to v2.

**Visualize in Kiali:**
- Open Kiali dashboard
- Navigate to Graph
- Select "microservices" namespace
- See traffic distribution

**Rollback to 100% v1:**
```bash
kubectl apply -f istio-configs/virtualservice-order.yaml
```

---

### Scenario 2: Blue-Green Deployment (Instant Switch)

**Purpose:** Instantly switch all traffic from v1 (blue) to v2 (green).

```bash
# Switch all traffic to v2
kubectl apply -f istio-scenarios/bluegreen-order.yaml

sleep 5

# Verify all traffic goes to v2
for i in {1..10}; do
  curl -s http://${INGRESS_HOST}/api/orders/test | jq -r '.version'
done
```

**Expected output:** All responses show `v2`

**Use case:** When you're confident v2 is stable and want immediate switch.

**Rollback to v1:**
```bash
kubectl apply -f istio-configs/virtualservice-order.yaml
```

---

### Scenario 3: A/B Testing (Header-based Routing)

**Purpose:** Route users to different versions based on headers.

```bash
# Apply A/B testing configuration
kubectl apply -f istio-scenarios/ab-testing-order.yaml

sleep 5

# Test regular users (should get v1)
curl -H "user-type: regular" http://${INGRESS_HOST}/api/orders/test | jq -r '.version'

# Test premium users (should get v2)
curl -H "user-type: premium" http://${INGRESS_HOST}/api/orders/test | jq -r '.version'
```

**Expected:**
- Regular users → v1
- Premium users → v2

**Use case:** Test new features with specific user segments.

**Test with multiple requests:**
```bash
# Regular users
for i in {1..5}; do
  curl -H "user-type: regular" http://${INGRESS_HOST}/api/orders/test | jq -r '.version'
done

# Premium users
for i in {1..5}; do
  curl -H "user-type: premium" http://${INGRESS_HOST}/api/orders/test | jq -r '.version'
done
```

---

### Scenario 4: Traffic Mirroring (Shadow Traffic)

**Purpose:** Send copy of production traffic to v2 for testing without affecting users.

```bash
# Apply traffic mirroring
kubectl apply -f istio-scenarios/mirror-order.yaml

sleep 5

# Send requests (production to v1, mirrored to v2)
for i in {1..10}; do
  curl -s http://${INGRESS_HOST}/api/orders/test > /dev/null
  echo "Request $i sent"
done

# Check v2 logs to see mirrored requests
kubectl logs -n microservices -l app=order-service,version=v2 -c order-service --tail=20
```

**Use case:** Test v2 with real production traffic without risk.

**Note:** Responses from mirrored traffic are discarded.

---

### Scenario 5: Fault Injection (Chaos Engineering)

**Purpose:** Test application resilience by injecting delays and errors.

```bash
# Apply fault injection (50% delay of 3s, 10% error rate)
kubectl apply -f istio-scenarios/fault-injection-product.yaml

sleep 5

# Test with injected faults
for i in {1..10}; do
  echo "Request $i:"
  time curl -s http://${INGRESS_HOST}/api/orders/create \
    -H "Content-Type: application/json" \
    -d '{"productId":"PROD-001","quantity":1}' | jq -r '.productName // "ERROR"'
  echo ""
done
```

**Expected:**
- ~50% of requests take 3+ seconds
- ~10% return errors (Product Not Available)

**Observe in Grafana:**
- See latency spikes
- Monitor error rates

**Clean up fault injection:**
```bash
kubectl delete -f istio-scenarios/fault-injection-product.yaml
kubectl apply -f istio-configs/virtualservice-product.yaml
```

---

### Scenario 6: Circuit Breaker

**Purpose:** Prevent cascading failures by breaking the circuit to unhealthy services.

```bash
# Apply aggressive circuit breaker settings
kubectl apply -f istio-scenarios/circuit-breaker-product.yaml

# Deploy fortio for load testing
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.20/samples/httpbin/sample-client/fortio-deploy.yaml -n microservices

# Wait for fortio pod
kubectl wait --for=condition=ready pod -l app=fortio -n microservices --timeout=60s

# Generate concurrent load to trip circuit breaker
kubectl exec -n microservices -c fortio \
  $(kubectl get pod -n microservices -l app=fortio -o jsonpath='{.items[0].metadata.name}') \
  -- fortio load -c 3 -qps 0 -n 30 -loglevel Warning \
  http://product-service:8080/api/products/PROD-001
```

**Expected:** Some requests fail as circuit breaker trips.

**Configuration details:**
- Max connections: 10
- Max pending requests: 5
- Consecutive errors before ejection: 2
- Ejection time: 30s

**Reset circuit breaker:**
```bash
kubectl apply -f istio-configs/destinationrule-product.yaml
```

---

### Scenario 7: Custom Timeout and Retry

**Purpose:** Configure custom timeouts and retry logic.

```bash
# Apply custom timeout/retry configuration
kubectl apply -f istio-scenarios/timeout-retry-order.yaml

sleep 5

# Test with multiple requests
for i in {1..5}; do
  echo "Request $i:"
  time curl -s http://${INGRESS_HOST}/api/orders/create \
    -H "Content-Type: application/json" \
    -d '{"productId":"PROD-002","quantity":1}' | jq -r '.orderId'
  echo ""
done
```

**Configuration:**
- Timeout: 3 seconds
- Retry attempts: 5
- Per-try timeout: 1 second
- Retry on: 5xx, reset, connection failures

**Use case:** Ensure requests don't hang indefinitely.

---

### Traffic Management Best Practices

**Gradual Rollout Strategy:**
```bash
# Step 1: Start with 5% canary
kubectl apply -f istio-scenarios/canary-order.yaml
# Edit to set v2 weight: 5

# Step 2: Monitor for 1 hour, increase to 25%
# Edit to set v2 weight: 25

# Step 3: Monitor for 2 hours, increase to 50%
# Edit to set v2 weight: 50

# Step 4: If stable, go 100% (blue-green)
kubectl apply -f istio-scenarios/bluegreen-order.yaml
```

**Monitor During Rollouts:**
- Watch Kiali for real-time traffic
- Check Grafana for latency/errors
- Review Jaeger for trace anomalies

---

## 📊 Part 8: Observability

### 8.1: Kiali - Service Mesh Visualization

**Start Kiali:**
```bash
kubectl port-forward svc/kiali -n istio-system 20001:20001
```

**Access:** http://localhost:20001

**Features:**
- **Graph:** Real-time service topology
    - Select namespace: `microservices`
    - Display: Versioned app graph
    - Traffic animation shows request flow
- **Applications:** List all applications
- **Workloads:** View deployments and pods
- **Services:** Service details and configs
- **Istio Config:** Validate configurations

**Generate Traffic for Visualization:**
```bash
# In a separate terminal
while true; do
  curl -s http://${INGRESS_HOST}/api/orders/test > /dev/null
  curl -s http://${INGRESS_HOST}/api/orders/create \
    -H "Content-Type: application/json" \
    -d '{"productId":"PROD-001","quantity":1}' > /dev/null
  sleep 1
done
```

**What to Observe:**
- Traffic flow: Ingress → Order → Product → PostgreSQL
- Request rates (requests per second)
- Error rates
- Response times

---

### 8.2: Grafana - Metrics Dashboards

**Start Grafana:**
```bash
kubectl port-forward svc/grafana -n istio-system 3000:3000
```

**Access:** http://localhost:3000

**Pre-configured Dashboards:**

1. **Istio Mesh Dashboard**
    - Path: Dashboards → Istio → Istio Mesh Dashboard
    - Shows: Global request volume, success rate, latency

2. **Istio Service Dashboard**
    - Path: Dashboards → Istio → Istio Service Dashboard
    - Select service: order-service or product-service
    - Shows: Request rates, latencies (p50, p90, p99), error rates

3. **Istio Workload Dashboard**
    - Path: Dashboards → Istio → Istio Workload Dashboard
    - Shows: Per-workload metrics

4. **Istio Performance Dashboard**
    - Shows: Istio component performance

**Key Metrics to Monitor:**
- **Request Rate:** Requests per second
- **Success Rate:** Percentage of successful requests
- **Duration:** p50, p90, p99 latencies
- **Incoming/Outgoing Bytes:** Network traffic

---

### 8.3: Jaeger - Distributed Tracing

**Start Jaeger:**
```bash
kubectl port-forward svc/tracing -n istio-system 16686:16686
```

**Access:** http://localhost:16686

**How to Use:**

1. **Search for Traces:**
    - Service: `order-service.microservices` or `product-service.microservices`
    - Lookback: Last hour
    - Click "Find Traces"

2. **Analyze a Trace:**
    - Click on a trace to see details
    - See span timeline showing:
        - order-service receiving request
        - order-service calling product-service
        - product-service querying database
        - Response times for each span

3. **Compare Traces:**
    - Compare slow vs. fast traces
    - Identify bottlenecks

**Example Trace Flow:**
```
istio-ingressgateway
  └─ order-service v1
      └─ product-service v1
          └─ PostgreSQL query
```

**What to Look For:**
- Total request duration
- Time spent in each service
- Database query times
- Network latencies between services

---

### 8.4: Prometheus - Raw Metrics

**Start Prometheus:**
```bash
kubectl port-forward svc/prometheus -n istio-system 9090:9090
```

**Access:** http://localhost:9090

**Useful Queries:**

```promql
# Total requests to order-service
istio_requests_total{destination_service_name="order-service"}

# Request rate (per second)
rate(istio_requests_total{destination_service_name="order-service"}[1m])

# Request duration 95th percentile
histogram_quantile(0.95, 
  rate(istio_request_duration_milliseconds_bucket{destination_service_name="order-service"}[1m])
)

# Success rate
sum(rate(istio_requests_total{destination_service_name="order-service",response_code!~"5.."}[1m])) 
/ 
sum(rate(istio_requests_total{destination_service_name="order-service"}[1m]))

# Error rate
sum(rate(istio_requests_total{destination_service_name="order-service",response_code=~"5.."}[1m]))

# TCP connections
istio_tcp_connections_opened_total{destination_service_name="product-service"}
```

**Create Alerts:**
- Error rate > 5%
- p95 latency > 500ms
- Request rate drops suddenly

---

### 8.5: View Logs

**Application Logs:**
```bash
# Order Service logs
kubectl logs -n microservices -l app=order-service,version=v1 -c order-service --tail=100

# Product Service logs
kubectl logs -n microservices -l app=product-service,version=v1 -c product-service --tail=100

# Follow logs in real-time
kubectl logs -n microservices -l app=order-service -c order-service -f
```

**Istio Proxy Logs:**
```bash
# Envoy access logs
kubectl logs -n microservices -l app=order-service -c istio-proxy --tail=50

# See all HTTP requests
kubectl logs -n microservices -l app=order-service -c istio-proxy | grep "HTTP"
```

**Check for Errors:**
```bash
kubectl logs -n microservices -l app=product-service -c product-service | grep -i error
```

---

### 8.6: Istio Diagnostics

**Proxy Status:**
```bash
istioctl proxy-status
```

**Expected output:**
```
NAME                                   CDS        LDS        EDS        RDS        ECDS         ISTIOD
order-service-v1-xxx.microservices     SYNCED     SYNCED     SYNCED     SYNCED     NOT SENT     istiod-xxx
order-service-v2-xxx.microservices     SYNCED     SYNCED     SYNCED     SYNCED     NOT SENT     istiod-xxx
product-service-v1-xxx.microservices   SYNCED     SYNCED     SYNCED     SYNCED     NOT SENT     istiod-xxx
product-service-v2-xxx.microservices   SYNCED     SYNCED     SYNCED     SYNCED     NOT SENT     istiod-xxx
```

**Proxy Configuration:**
```bash
PODNAME=$(kubectl get pod -n microservices -l app=order-service,version=v1 -o jsonpath='{.items[0].metadata.name}')

# View clusters (upstream services)
istioctl proxy-config cluster $PODNAME -n microservices

# View routes
istioctl proxy-config route $PODNAME -n microservices

# View listeners
istioctl proxy-config listener $PODNAME -n microservices

# View endpoints
istioctl proxy-config endpoint $PODNAME -n microservices
```

**Analyze Configuration Issues:**
```bash
istioctl analyze -n microservices
```

**Get Envoy Stats:**
```bash
kubectl exec -n microservices $PODNAME -c istio-proxy -- curl -s localhost:15000/stats/prometheus | grep istio_requests_total
```

---

## 🔒 Part 9: Security Features

### 9.1: Verify Mutual TLS (mTLS)

**Check mTLS Status:**
```bash
PODNAME=$(kubectl get pod -n microservices -l app=order-service -o jsonpath='{.items[0].metadata.name}')

istioctl authn tls-check $PODNAME -n microservices
```

**Expected output:**
```
HOST:PORT                                    STATUS     SERVER     CLIENT     AUTHN POLICY     DESTINATION RULE
product-service.microservices.svc.cluster... OK         STRICT     ISTIO      default/...      product-service/...
```

**What this means:**
- All service-to-service communication is encrypted
- Certificates are automatically rotated
- No code changes required

**Test mTLS:**
```bash
# Try to connect without mTLS (should fail)
kubectl run test --image=curlimages/curl -it --rm -n default -- \
  curl http://order-service.microservices:8080/api/orders/test

# Expected: Connection refused or timeout
```

---

### 9.2: Authorization Policies

**Current Policies:**

1. **Allow Ingress → Order Service**
    - Source: istio-system namespace
    - Destination: order-service
    - Methods: GET, POST

2. **Allow Order → Product Service**
    - Source: microservices namespace
    - Destination: product-service
    - Paths: /api/products/*
    - Methods: GET, POST

**Test Authorization:**
```bash
# This should work (allowed by policy)
curl http://${INGRESS_HOST}/api/orders/test

# Try to access product-service directly (should be blocked)
kubectl run test --image=curlimages/curl -it --rm -n default -- \
  curl http://product-service.microservices:8080/api/products/PROD-001

# Expected: RBAC: access denied
```

**Create Custom Authorization Policy:**
```yaml
# deny-delete.yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: deny-delete
  namespace: microservices
spec:
  selector:
    matchLabels:
      app: product-service
  action: DENY
  rules:
  - to:
    - operation:
        methods: ["DELETE"]
```

```bash
kubectl apply -f deny-delete.yaml
```

---

### 9.3: Request Authentication (JWT)

**Apply JWT Policy:**
```yaml
# jwt-auth.yaml
apiVersion: security.istio.io/v1beta1
kind: RequestAuthentication
metadata:
  name: jwt-order-service
  namespace: microservices
spec:
  selector:
    matchLabels:
      app: order-service
  jwtRules:
  - issuer: "testing@secure.istio.io"
    jwksUri: "https://raw.githubusercontent.com/istio/istio/release-1.20/security/tools/jwt/samples/jwks.json"
```

```bash
kubectl apply -f jwt-auth.yaml
```

**Test with JWT:**
```bash
# Get sample JWT token
TOKEN=$(curl https://raw.githubusercontent.com/istio/istio/release-1.20/security/tools/jwt/samples/demo.jwt -s)

# Request with valid JWT
curl -H "Authorization: Bearer $TOKEN" http://${INGRESS_HOST}/api/orders/test

# Request without JWT (still allowed by default)
curl http://${INGRESS_HOST}/api/orders/test
```

**Require JWT (enforce):**
```yaml
# require-jwt.yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: require-jwt
  namespace: microservices
spec:
  selector:
    matchLabels:
      app: order-service
  action: DENY
  rules:
  - from:
    - source:
        notRequestPrincipals: ["*"]
```

---

## 🔧 Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n microservices

# Describe pod for events
kubectl describe pod <pod-name> -n microservices

# Check logs
kubectl logs <pod-name> -n microservices -c order-service
kubectl logs <pod-name> -n microservices -c istio-proxy

# Common issues:
# - Image pull errors: Check ACR permissions
# - Database connection: Verify postgres-secret
# - Resource limits: Check node capacity
```

### Istio Sidecar Not Injected

```bash
# Check namespace label
kubectl get namespace microservices --show-labels

# Should have: istio-injection=enabled

# Re-label if needed
kubectl label namespace microservices istio-injection=enabled --overwrite

# Restart pods
kubectl rollout restart deployment -n microservices
```

### Service Not Accessible

```bash
# Check ingress gateway
kubectl get svc -n istio-system istio-ingressgateway

# Check if external IP is assigned
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo $INGRESS_HOST

# Check gateway configuration
kubectl get gateway -n microservices
kubectl describe gateway microservices-gateway -n microservices

# Check virtual service
kubectl get virtualservice -n microservices
kubectl describe virtualservice order-service -n microservices
```

### Database Connection Issues

```bash
# Check secret
kubectl get secret postgres-credentials -n microservices -o yaml

# Test connection from pod
kubectl exec -it <product-service-pod> -n microservices -c product-service -- sh
nc -zv <POSTGRES_SERVER> 5432
exit

# Check product service logs
kubectl logs -n microservices -l app=product-service -c product-service | grep -i postgres
```

### Istio Configuration Issues

```bash
# Analyze configuration
istioctl analyze -n microservices

# Check proxy sync status
istioctl proxy-status

# Validate install
istioctl verify-install

# Check control plane logs
kubectl logs -n istio-system -l app=istiod
```

### Performance Issues

```bash
# Check resource usage
kubectl top nodes
kubectl top pods -n microservices

# Check for throttling
kubectl describe pod <pod-name> -n microservices | grep -i throttl

# Increase resources if needed
kubectl edit deployment order-service-v1 -n microservices
```

### Traffic Not Routing Correctly

```bash
# Check destination rules
kubectl get destinationrule -n microservices
kubectl describe destinationrule order-service -n microservices

# Verify pod labels
kubectl get pods -n microservices --show-labels

# Check virtual service routes
kubectl get virtualservice order-service -n microservices -o yaml
```

---

## 🧹 Cleanup

### Option 1: Delete Everything

```bash
source azure-resources.env

# Delete Azure resource group (deletes everything)
az group delete --name $RESOURCE_GROUP --yes --no-wait

# This deletes:
# - AKS cluster
# - PostgreSQL server
# - Container Registry
# - All associated resources
```

**Time:** Resources marked for deletion immediately, actual deletion takes ~10 minutes

**Cost:** Stops billing immediately

---

### Option 2: Keep Infrastructure, Remove Apps

```bash
# Delete applications
kubectl delete namespace microservices

# Delete Istio
cd istio-1.*
istioctl uninstall --purge -y
kubectl delete namespace istio-system
cd ..

# Cluster remains for other use
```

---

### Option 3: Selective Cleanup

```bash
# Delete only Istio scenarios (keep base config)
kubectl delete -f istio-scenarios/

# Delete specific application version
kubectl delete -f k8s-manifests/order-service-v2.yaml

# Delete observability addons
kubectl delete -f istio-1.*/samples/addons/
```

---

## 💰 Cost Estimation

### Daily Costs (approximate, USD)

| Resource | SKU | Daily Cost |
|----------|-----|------------|
| AKS Cluster (3 nodes) | Standard_D2s_v3 | $4.80 |
| PostgreSQL | B_Gen5_1 | $1.20 |
| Container Registry | Basic | $0.17 |
| Load Balancer | Standard | $0.60 |
| Public IP | Standard | $0.12 |
| **Total** | | **~$7/day** |

### Monthly Costs (if left running)
- **~$210/month**

### Cost Optimization Tips

1. **Use for learning, then delete:**
   ```bash
   # Run for 4 hours of learning
   # Cost: ~$1.20
   az group delete --name $RESOURCE_GROUP --yes
   ```

2. **Stop AKS cluster when not in use:**
   ```bash
   az aks stop --name $AKS_CLUSTER_NAME --resource-group $RESOURCE_GROUP
   # Restart when needed:
   az aks start --name $AKS_CLUSTER_NAME --resource-group $RESOURCE_GROUP
   ```

3. **Use smaller PostgreSQL SKU:**
   ```bash
   # B_Gen5_1 is minimum
   # Already using cheapest option
   ```

4. **Delete when done:**
    - Most cost-effective for learning
    - Can recreate in 30 minutes

---

## 📚 Additional Resources

### Official Documentation
- [Istio Documentation](https://istio.io/latest/docs/)
- [Azure AKS Documentation](https://docs.microsoft.com/azure/aks/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

### Istio Learning
- [Istio Tasks](https://istio.io/latest/docs/tasks/) - Step-by-step guides
- [Istio Examples](https://istio.io/latest/docs/examples/) - Sample applications
- [Istio Best Practices](https://istio.io/latest/docs/ops/best-practices/)

### Monitoring & Observability
- [Kiali Documentation](https://kiali.io/docs/)
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [Prometheus Query Examples](https://prometheus.io/docs/prometheus/latest/querying/examples/)

### Advanced Topics
- [Istio Security](https://istio.io/latest/docs/concepts/security/)
- [Multi-cluster Istio](https://istio.io/latest/docs/setup/install/multicluster/)
- [Istio Performance Tuning](https://istio.io/latest/docs/ops/deployment/performance-and-scalability/)

---

## 🎓 Learning Path

### Beginner (Day 1)
1. ✅ Complete infrastructure setup
2. ✅ Deploy applications
3. ✅ Test basic endpoints
4. ✅ Open Kiali and watch traffic flow

### Intermediate (Day 2)
1. ✅ Try canary deployment
2. ✅ Test A/B routing
3. ✅ Explore Grafana dashboards
4. ✅ View traces in Jaeger

### Advanced (Day 3)
1. ✅ Implement circuit breaker
2. ✅ Configure fault injection
3. ✅ Test authorization policies
4. ✅ Create custom metrics and alerts

---

## 🤝 Contributing

Found an issue or want to improve this guide?
1. Report issues with detailed steps to reproduce
2. Suggest improvements for clarity
3. Share your learnings and tips

---

## 📝 License

This project is for educational purposes.

---

## ✨ What You've Learned

By completing this guide, you now know how to:

- ✅ Deploy microservices on Azure AKS
- ✅ Install and configure Istio service mesh
- ✅ Implement advanced traffic management patterns
- ✅ Set up comprehensive observability
- ✅ Secure service-to-service communication with mTLS
- ✅ Monitor and troubleshoot distributed systems
- ✅ Perform chaos engineering with fault injection
- ✅ Implement progressive delivery strategies

**Next Steps:**
- Explore multi-cluster Istio setups
- Implement GitOps with ArgoCD
- Add custom metrics and alerting
- Integrate with external systems
- Deploy to production with proper monitoring

---

**Happy Learning! 🚀**

If you found this guide helpful, please share it with others learning Istio and Kubernetes!