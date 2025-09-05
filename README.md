# Java Hello World Web Server

A simple Java web server that displays "Hello! World" on a web page, with automated Docker and Kubernetes deployment via GitHub Actions.

## Files

- `HelloWorldServer.java` - Java web server application
- `Dockerfile` - Docker configuration with Ubuntu base
- `.github/workflows/docker-build-deploy.yml` - CI/CD pipeline for Docker and Kubernetes
- `k8s/` - Kubernetes manifests (namespace, deployment, service, ingress)

## Quick Start

### Run from Docker Hub
```bash
# Replace 'your-dockerhub-username' with your Docker Hub username
docker run -p 8080:8080 your-dockerhub-username/java-hello-world:latest
```

Visit `http://localhost:8080` to see "Hello! World"

### Local Development
```bash
# Compile and run locally
javac HelloWorldServer.java
java HelloWorldServer
```

## GitHub Actions Setup

### Required Secrets

Add these secrets to your GitHub repository (Settings → Secrets and variables → Actions):

1. **Docker Hub:**
   - `DOCKER_HUB_USERNAME` - Your Docker Hub username
   - `DOCKER_HUB_ACCESS_TOKEN` - Docker Hub access token

2. **Kubernetes:**
   - `KUBE_CONFIG` - Base64 encoded kubeconfig file for your cluster

### Getting Kubernetes Config

```bash
# Encode your kubeconfig for GitHub secrets
cat ~/.kube/config | base64 -w 0
```

### Deployment

- Push to `main` branch
- GitHub Actions automatically:
  - Builds and pushes Docker image to Docker Hub
  - Deploys to Kubernetes cluster with 3 replicas
  - Creates LoadBalancer service and ingress

## Kubernetes Deployment

### Manual Deployment

```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n java-hello-world
kubectl get services -n java-hello-world
```

### Access the Application

- **LoadBalancer:** Get external IP with `kubectl get service java-hello-world-service -n java-hello-world`
- **Ingress:** Add `java-hello-world.local` to your `/etc/hosts` pointing to ingress IP

## What It Does

- **Web Server:** Serves HTML page with "Hello! World" on port 8080
- **Docker:** Ubuntu-based container with OpenJDK 17
- **Kubernetes:** Deploys with 3 replicas, health checks, resource limits
- **CI/CD:** Automatic build and deployment to Docker Hub and Kubernetes
- **Multi-platform:** Supports linux/amd64 and linux/arm64
- **Production Ready:** LoadBalancer service, ingress, and monitoring