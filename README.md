# Java Hello World Web Server

A simple Java web server that displays "Hello! World" on a web page, with automated Docker deployment via GitHub Actions.

## Files

- `HelloWorldServer.java` - Java web server application
- `Dockerfile` - Docker configuration with Ubuntu base
- `.github/workflows/docker-build-deploy.yml` - CI/CD pipeline

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

1. **Create Docker Hub Access Token:**
   - Docker Hub → Account Settings → Security → New Access Token

2. **Add GitHub Secrets:**
   - Repository → Settings → Secrets and variables → Actions
   - Add: `DOCKER_HUB_USERNAME` and `DOCKER_HUB_ACCESS_TOKEN`

3. **Deploy:**
   - Push to `main` branch.
   - GitHub Actions automatically builds and pushes to Docker Hub

## What It Does

- **Web Server:** Serves HTML page with "Hello! World" on port 8080
- **Docker:** Ubuntu-based container with OpenJDK 17
- **CI/CD:** Automatic build and deployment on push to main
- **Multi-platform:** Supports linux/amd64 and linux/arm64