# Java Hello World Web Server - Complete Code Documentation

A comprehensive Java web server application with automated Docker containerization and Kubernetes deployment via GitHub Actions CI/CD pipeline.

## Project Overview

This project demonstrates a complete DevOps pipeline that:
- Builds a simple Java HTTP server
- Containerizes it using Docker
- Deploys to Kubernetes using GitHub Actions
- Includes health checks, load balancing, and ingress routing

## Project Structure

```
Github-Action-POC/
├── .github/workflows/
│   └── docker-build-deploy.yml    # GitHub Actions CI/CD pipeline
├── k8s/
│   ├── namespace.yaml             # Kubernetes namespace definition
│   ├── deployment.yaml            # Kubernetes deployment configuration
│   ├── service.yaml               # Kubernetes service (LoadBalancer)
│   └── ingress.yaml               # Kubernetes ingress routing
├── Dockerfile                     # Docker container configuration
├── HelloWorldServer.java          # Java web server application
└── README.md                      # This documentation file
```

## File-by-File Code Analysis

### 1. HelloWorldServer.java - Java Web Server Application

This file contains the main Java application that creates an HTTP server.

```java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
```
**Lines 1-6:** Import statements for Java's built-in HTTP server functionality:
- `HttpServer`: Creates and manages the HTTP server
- `HttpHandler`: Interface for handling HTTP requests
- `HttpExchange`: Represents an HTTP request-response exchange
- `IOException`: Exception handling for I/O operations
- `OutputStream`: For writing response data
- `InetSocketAddress`: For specifying server address and port

```java
public class HelloWorldServer {
```
**Line 8:** Class declaration for the main server class

```java
    public static void main(String[] args) throws IOException {
```
**Line 9:** Main method entry point, declares IOException to handle network errors

```java
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
```
**Line 10:** Creates HTTP server instance:
- `HttpServer.create()`: Factory method to create server
- `new InetSocketAddress(8080)`: Binds server to port 8080 on all interfaces
- `0`: Uses default backlog (maximum number of incoming connections)

```java
        server.createContext("/", new HelloWorldHandler());
```
**Line 11:** Maps the root path "/" to HelloWorldHandler class for request processing

```java
        server.setExecutor(null);
```
**Line 12:** Uses default executor (creates thread pool automatically for handling requests)

```java
        System.out.println("Server started on http://localhost:8080");
```
**Line 13:** Prints startup message to console for debugging

```java
        server.start();
```
**Line 14:** Starts the HTTP server (non-blocking call)

```java
    static class HelloWorldHandler implements HttpHandler {
```
**Line 17:** Inner static class that implements HttpHandler interface for request processing

```java
        @Override
        public void handle(HttpExchange exchange) throws IOException {
```
**Line 19:** Override method that handles all HTTP requests to the "/" context

```java
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Hello World Java App</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; margin-top: 100px; }
                        h1 { color: #333; font-size: 3em; }
                    </style>
                </head>
                <body>
                    <h1>Hello! World</h1>
                    <p>Java application running in Docker with Ubuntu</p>
                </body>
                </html>
                """;
```
**Lines 20-35:** Text block (Java 15+ feature) containing HTML response:
- Complete HTML document with DOCTYPE declaration
- CSS styling for centered layout with Arial font
- Main heading "Hello! World" in large gray text
- Descriptive paragraph about the application environment

```java
            exchange.getResponseHeaders().set("Content-Type", "text/html");
```
**Line 37:** Sets HTTP response header to indicate HTML content type

```java
            exchange.sendResponseHeaders(200, response.getBytes().length);
```
**Line 38:** Sends HTTP response headers:
- `200`: HTTP OK status code
- `response.getBytes().length`: Content length in bytes

```java
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
```
**Lines 39-41:** Writes response body and closes the output stream:
- Gets output stream from the exchange
- Writes HTML response as bytes
- Closes stream to complete the response

### 2. Dockerfile - Container Configuration

This file defines how to build the Docker container for the Java application.

```dockerfile
# Use Ubuntu as base image
FROM ubuntu:22.04
```
**Lines 1-2:** 
- Comment explaining base image choice
- Uses Ubuntu 22.04 LTS as the foundation (stable, long-term support)

```dockerfile
# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive
```
**Lines 4-5:**
- Comment describing environment setup
- Prevents interactive prompts during package installation

```dockerfile
# Update package list and install OpenJDK 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```
**Lines 7-11:** Multi-line RUN command for Java installation:
- `apt-get update`: Updates package repository lists
- `apt-get install -y openjdk-17-jdk`: Installs Java 17 JDK (LTS version)
- `apt-get clean`: Removes downloaded package files
- `rm -rf /var/lib/apt/lists/*`: Deletes cached package lists to reduce image size

```dockerfile
# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```
**Lines 13-14:**
- Comment explaining Java environment setup
- Sets JAVA_HOME environment variable for Java applications

```dockerfile
# Create app directory
WORKDIR /app
```
**Lines 16-17:**
- Comment describing working directory creation
- Sets `/app` as the working directory for subsequent commands

```dockerfile
# Copy Java source file
COPY HelloWorldServer.java .
```
**Lines 19-20:**
- Comment explaining file copy operation
- Copies Java source file from host to container's `/app` directory

```dockerfile
# Compile Java application
RUN javac HelloWorldServer.java
```
**Lines 22-23:**
- Comment describing compilation step
- Compiles Java source code into bytecode (.class file)

```dockerfile
# Expose port 8080
EXPOSE 8080
```
**Lines 25-26:**
- Comment explaining port exposure
- Documents that container listens on port 8080 (metadata for Docker)

```dockerfile
# Run the web server
CMD ["java", "HelloWorldServer"]
```
**Lines 28-29:**
- Comment describing container startup command
- Executes Java application when container starts (exec form for better signal handling)

### 3. GitHub Actions Workflow (.github/workflows/docker-build-deploy.yml)

This file defines the CI/CD pipeline that automatically builds, tests, deploys the application, and sends Slack notifications.

```yaml
name: Build and Deploy to Docker Hub and Kubernetes
```
**Line 1:** Workflow name displayed in GitHub Actions interface

```yaml
on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main
```
**Lines 2-8:** Trigger configuration:
- Runs on pushes to main branch
- Runs on pull requests targeting main branch
- Ensures code is tested before merging

```yaml
env:
  DOCKER_IMAGE_NAME: java-hello-world
```
**Lines 10-11:** Environment variables:
- Defines Docker image name used throughout the workflow
- Centralized configuration for easy maintenance

```yaml
jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
```
**Lines 13-15:** Job configuration:
- Single job named "build-and-deploy"
- Runs on latest Ubuntu runner (GitHub-hosted)

```yaml
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
```
**Lines 17-19:** First step - source code checkout:
- Uses official GitHub action to download repository code
- v4 is the latest stable version

```yaml
    - name: Set up Java JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
```
**Lines 21-25:** Java environment setup:
- Uses official Java setup action
- Installs Java 17 (LTS version matching Dockerfile)
- Uses Eclipse Temurin distribution (open-source OpenJDK)

```yaml
    - name: Compile Java application
      run: javac HelloWorldServer.java
```
**Lines 27-28:** Java compilation step:
- Compiles source code to verify it builds correctly
- Catches compilation errors before Docker build

```yaml
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3
```
**Lines 30-31:** Docker Buildx setup:
- Enables advanced Docker build features
- Required for multi-platform builds

```yaml
    - name: Log in to Docker Hub
      uses: docker/login-action@v3
      with:
        username: ${{ secrets.DOCKER_HUB_USERNAME }}
        password: ${{ secrets.DOCKER_HUB_ACCESS_TOKEN }}
```
**Lines 33-37:** Docker Hub authentication:
- Uses official Docker login action
- Credentials stored as GitHub repository secrets
- Access token used instead of password for security

```yaml
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ secrets.DOCKER_HUB_USERNAME }}/${{ env.DOCKER_IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix=commit-
          type=raw,value=latest,enable={{is_default_branch}}
```
**Lines 39-48:** Docker image metadata generation:
- Creates tags and labels for Docker image
- `type=ref,event=branch`: Tags with branch name
- `type=ref,event=pr`: Tags with PR number
- `type=sha,prefix=commit-`: Tags with Git commit SHA
- `type=raw,value=latest`: Tags as "latest" only on main branch

```yaml
    - name: Build and push Docker image
      id: build
      uses: docker/build-push-action@v5
      with:
        context: .
        file: ./Dockerfile
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        platforms: linux/amd64,linux/arm64
```
**Lines 50-59:** Docker build and push:
- Builds Docker image using Dockerfile
- Pushes to Docker Hub with generated tags
- Supports multiple architectures (AMD64 and ARM64)
- Uses build context from repository root

```yaml
    - name: Set up kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'v1.28.0'
```
**Lines 61-64:** Kubernetes CLI setup:
- Installs kubectl for Kubernetes operations
- Uses specific version for consistency

```yaml
    - name: Configure kubectl
      run: |
        mkdir -p ~/.kube
        printf '%s\n' "${{ secrets.KUBE_CONFIG }}" > ~/.kube/config
        chmod 600 ~/.kube/config
```
**Lines 66-70:** Kubernetes configuration:
- Creates kubectl configuration directory
- Writes base64-encoded kubeconfig from secrets
- Sets secure file permissions (owner read/write only)

```yaml
    - name: Update Kubernetes deployment image
      run: |
        # Replace IMAGE_PLACEHOLDER with actual image
        sed -i "s|IMAGE_PLACEHOLDER|${{ secrets.DOCKER_HUB_USERNAME }}/${{ env.DOCKER_IMAGE_NAME }}:latest|g" k8s/deployment.yaml
```
**Lines 73-76:** Dynamic image reference update:
- Uses sed command to replace placeholder with actual image name
- Enables template-based deployment configuration
- Updates deployment.yaml with built image reference

```yaml
    - name: Deploy to Kubernetes
      run: |
        kubectl apply -f k8s/namespace.yaml
        kubectl apply -f k8s/deployment.yaml
        kubectl apply -f k8s/service.yaml
        kubectl apply -f k8s/ingress.yaml
```
**Lines 78-83:** Kubernetes deployment:
- Applies all Kubernetes manifests in order
- Creates namespace, deployment, service, and ingress
- Uses declarative configuration approach

```yaml
    - name: Wait for deployment
      run: |
        kubectl wait --for=condition=available --timeout=300s deployment/java-hello-world -n java-hello-world
```
**Lines 85-87:** Deployment verification:
- Waits for deployment to become available
- 300-second timeout prevents infinite waiting
- Ensures pods are running before continuing

```yaml
    - name: Get deployment status
      run: |
        kubectl get pods -n java-hello-world
        kubectl get services -n java-hello-world
        kubectl get ingress -n java-hello-world
```
**Lines 89-93:** Status reporting:
- Shows running pods in the namespace
- Displays service configuration and external IPs
- Shows ingress routing rules

```yaml
    - name: Deploy summary
      run: |
        echo "## Deployment Summary" >> $GITHUB_STEP_SUMMARY
        echo "Java web server application compiled successfully" >> $GITHUB_STEP_SUMMARY
        echo "Docker image built and pushed to Docker Hub" >> $GITHUB_STEP_SUMMARY
        echo "Application deployed to Kubernetes cluster" >> $GITHUB_STEP_SUMMARY
        echo "**Image:** \`${{ secrets.DOCKER_HUB_USERNAME }}/${{ env.DOCKER_IMAGE_NAME }}:latest\`" >> $GITHUB_STEP_SUMMARY
        echo "**Docker Hub:** https://hub.docker.com/r/${{ secrets.DOCKER_HUB_USERNAME }}/${{ env.DOCKER_IMAGE_NAME }}" >> $GITHUB_STEP_SUMMARY
        echo "**Kubernetes:** Deployed to namespace \`java-hello-world\`" >> $GITHUB_STEP_SUMMARY
```
**Lines 95-103:** Deployment summary creation:
- Creates formatted summary for GitHub Actions interface
- Uses GitHub-flavored Markdown for rich formatting
- Includes links and status indicators
- Provides deployment details for team visibility

```yaml
    - name: Notify Slack on successful build
      if: success() && github.ref == 'refs/heads/main'
      uses: 8398a7/action-slack@v3
      with:
        status: success
        text: |
          ✅ Build and deployment completed successfully!
          
          **Repository:** ${{ github.repository }}
          **Branch:** ${{ github.ref_name }}
          **Commit:** ${{ github.sha }}
          **Author:** ${{ github.actor }}
          **Image:** ${{ secrets.DOCKER_HUB_USERNAME }}/${{ env.DOCKER_IMAGE_NAME }}:latest
          **Kubernetes:** Deployed to namespace `java-hello-world`
      env:
        SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```
**Lines 105-120:** Slack success notification:
- Triggers only on successful builds to main branch
- Uses community Slack action for webhook integration
- Includes comprehensive deployment information
- Formatted with emojis and markdown for readability

```yaml
    - name: Notify Slack on build failure
      if: failure() && github.ref == 'refs/heads/main'
      uses: 8398a7/action-slack@v3
      with:
        status: failure
        text: |
          ❌ Build or deployment failed!
          
          **Repository:** ${{ github.repository }}
          **Branch:** ${{ github.ref_name }}
          **Commit:** ${{ github.sha }}
          **Author:** ${{ github.actor }}
          **Workflow:** ${{ github.workflow }}
          
          Please check the GitHub Actions logs for details.
      env:
        SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```
**Lines 122-137:** Slack failure notification:
- Triggers only on failed builds to main branch
- Provides essential debugging information
- Directs users to GitHub Actions logs for troubleshooting
- Uses webhook URL stored in repository secrets

### 4. Kubernetes Manifests (k8s/ directory)

#### 4.1 namespace.yaml - Kubernetes Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: java-hello-world
  labels:
    name: java-hello-world
```
**Line 1:** Kubernetes API version for core resources
**Line 2:** Resource type - Namespace for logical separation
**Line 4:** Namespace name used across all resources
**Line 6:** Label for resource identification and selection

#### 4.2 deployment.yaml - Application Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-hello-world
  namespace: java-hello-world
  labels:
    app: java-hello-world
```
**Lines 1-7:** Deployment metadata:
- Uses apps/v1 API for deployment resources
- Defines deployment name and target namespace
- Labels enable resource grouping and selection

```yaml
spec:
  replicas: 3
```
**Lines 8-9:** Deployment specification:
- Creates 3 identical pod replicas for high availability
- Enables load distribution and fault tolerance

```yaml
  selector:
    matchLabels:
      app: java-hello-world
```
**Lines 10-12:** Pod selector:
- Defines which pods this deployment manages
- Matches pods with "app: java-hello-world" label

```yaml
  template:
    metadata:
      labels:
        app: java-hello-world
```
**Lines 13-16:** Pod template metadata:
- Template for creating pods
- Labels must match selector for proper management

```yaml
    spec:
      containers:
      - name: java-hello-world
        image: IMAGE_PLACEHOLDER
        ports:
        - containerPort: 8080
```
**Lines 17-22:** Container specification:
- Single container per pod
- Image placeholder replaced during deployment
- Exposes port 8080 for HTTP traffic

```yaml
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```
**Lines 23-29:** Resource management:
- `requests`: Minimum guaranteed resources
- `limits`: Maximum allowed resources
- Prevents resource starvation and overconsumption

```yaml
        livenessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```
**Lines 30-35:** Liveness probe configuration:
- Checks if container is alive by HTTP GET to "/"
- Waits 30 seconds before first check (startup time)
- Checks every 10 seconds thereafter
- Kubernetes restarts container if probe fails

```yaml
        readinessProbe:
          httpGet:
            path: /
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```
**Lines 36-41:** Readiness probe configuration:
- Checks if container is ready to receive traffic
- Shorter delays than liveness probe
- Kubernetes removes pod from service if probe fails
- Faster recovery for temporary issues

#### 4.3 service.yaml - Load Balancer Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: java-hello-world-service
  namespace: java-hello-world
  labels:
    app: java-hello-world
```
**Lines 1-7:** Service metadata:
- Core API version for service resources
- Service name used by other resources
- Same namespace and labels as deployment

```yaml
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
```
**Lines 8-14:** Service specification:
- `LoadBalancer`: Provisions external load balancer (cloud provider)
- Maps external port 80 to container port 8080
- TCP protocol for HTTP traffic
- Named port for reference in other resources

```yaml
  selector:
    app: java-hello-world
```
**Lines 15-16:** Pod selector:
- Routes traffic to pods with matching label
- Load balances across all healthy replicas

#### 4.4 ingress.yaml - HTTP Routing

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: java-hello-world-ingress
  namespace: java-hello-world
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/rewrite-target: /
```
**Lines 1-8:** Ingress metadata and annotations:
- Networking API for ingress resources
- Nginx ingress controller configuration
- Rewrites incoming paths to root path "/"

```yaml
spec:
  rules:
  - host: java-hello-world.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: java-hello-world-service
            port:
              number: 80
```
**Lines 9-20:** Ingress routing rules:
- Routes requests for "java-hello-world.local" host
- Matches all paths starting with "/" (prefix match)
- Forwards traffic to the LoadBalancer service on port 80
- Enables hostname-based routing and SSL termination

## Quick Start Guide

### Prerequisites
- Java 17 or higher
- Docker (for containerization)
- kubectl (for Kubernetes deployment)
- Access to Kubernetes cluster

### Local Development
```bash
# Compile and run locally
javac HelloWorldServer.java
java HelloWorldServer
# Visit http://localhost:8080
```

### Docker Deployment
```bash
# Build Docker image
docker build -t java-hello-world .

# Run container
docker run -p 8080:8080 java-hello-world
```

### Kubernetes Deployment
```bash
# Deploy all resources
kubectl apply -f k8s/

# Check deployment status
kubectl get all -n java-hello-world

# Get service external IP
kubectl get service java-hello-world-service -n java-hello-world
```

## GitHub Actions Setup

### Required Repository Secrets
1. **DOCKER_HUB_USERNAME** - Your Docker Hub username
2. **DOCKER_HUB_ACCESS_TOKEN** - Docker Hub access token
3. **KUBE_CONFIG** - Base64-encoded kubeconfig file
4. **SLACK_WEBHOOK_URL** - Slack webhook URL for build notifications

### Getting Kubernetes Config
```bash
# Encode your kubeconfig
cat ~/.kube/config | base64 -w 0
```

### Setting up Slack Webhook
1. Go to your Slack workspace
2. Navigate to **Apps** → **Incoming Webhooks**
3. Create a new webhook for your desired channel
4. Copy the webhook URL (format: `https://hooks.slack.com/services/T.../B.../...`)
5. Add it as `SLACK_WEBHOOK_URL` secret in your GitHub repository

### Slack Notifications
The workflow automatically sends Slack notifications for:
- ✅ **Successful builds and deployments** on main branch
- ❌ **Failed builds or deployments** on main branch

Notifications include:
- Repository and branch information
- Commit SHA and author details
- Docker image details and Kubernetes deployment status
- Links to GitHub Actions logs for failures

## Architecture Overview

### Application Flow
1. **Source Code**: Java HTTP server with embedded HTML response
2. **Containerization**: Ubuntu-based Docker image with OpenJDK 17
3. **CI/CD Pipeline**: GitHub Actions builds and deploys automatically
4. **Kubernetes Deployment**: 3 replicas with health checks and resource limits
5. **Load Balancing**: Service distributes traffic across healthy pods
6. **Ingress Routing**: Domain-based routing with potential SSL termination

### Production Features
- **High Availability**: 3 replicas ensure service continuity
- **Health Monitoring**: Liveness and readiness probes
- **Resource Management**: CPU and memory limits prevent resource starvation
- **Multi-platform Support**: AMD64 and ARM64 architectures
- **Automated Deployment**: Push-to-deploy workflow
- **External Access**: LoadBalancer service and ingress routing

## Troubleshooting

### Common Issues
1. **Port conflicts**: Change port in Java code and Dockerfile
2. **Resource limits**: Adjust requests/limits in deployment.yaml
3. **Image pull errors**: Verify Docker Hub credentials and image name
4. **Service unavailable**: Check pod status and health probes
5. **Ingress not working**: Verify ingress controller and DNS configuration

### Debugging Commands
```bash
# Check pod logs
kubectl logs -f deployment/java-hello-world -n java-hello-world

# Describe deployment for events
kubectl describe deployment java-hello-world -n java-hello-world

# Check service endpoints
kubectl get endpoints java-hello-world-service -n java-hello-world

# Test connectivity
kubectl port-forward service/java-hello-world-service 8080:80 -n java-hello-world
```

This documentation provides a complete understanding of every line of code in the project, enabling developers to modify, extend, and maintain the application effectively.