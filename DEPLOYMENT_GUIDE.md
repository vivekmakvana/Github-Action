# Enterprise CI/CD Deployment Guide

## Overview

This repository now uses an enterprise-grade CI/CD pipeline with proper environment separation, security scanning, and approval gates.

## Branching Strategy

### Branch Structure
- `main` - Production-ready code, protected branch
- `develop` - Integration branch for features  
- `feature/*` - Feature development branches
- `hotfix/*` - Emergency production fixes

### Environment Mapping
- **Development**: `develop` branch → Auto-deploy
- **Staging**: `main` branch → Auto-deploy  
- **Production**: Manual deployment only (any image tag)

## Workflows

### 1. PR Build Validation (`pr-validation.yml`)
**Triggers**: Pull requests to main/develop branches only
**Purpose**: Validate code changes before merging

**What it does**:
- Compiles Java application
- Runs smoke tests
- Builds Docker image locally (validation only)
- Tests Docker container functionality
- Validates Kubernetes manifests
- Does NOT publish images (validation only)

### 2. CI Build (`ci-build.yml`)
**Triggers**: Push to main, develop, feature/*, hotfix/* branches (NOT PRs)
**Purpose**: Build, test, scan, and publish Docker images

**What it does**:
- Compiles Java application
- Runs smoke tests  
- Builds multi-platform Docker images
- Runs security scans with Trivy
- Validates Kubernetes manifests
- Publishes images to GitHub Container Registry

### 3. Development Deployment (`deploy-dev.yml`)
**Triggers**: Push to `develop` branch, manual dispatch
**Purpose**: Auto-deploy to development environment

**Features**:
- Automatic deployment on develop branch updates
- Development-specific namespace (`java-hello-world-dev`)
- Basic smoke testing
- Manual deployment option with custom image tags

### 4. Staging Deployment (`deploy-staging.yml`)  
**Triggers**: Push to `main` branch, manual dispatch
**Purpose**: Auto-deploy to staging for UAT

**Features**:
- Automatic deployment on main branch updates
- Staging-specific namespace (`java-hello-world-staging`) 
- Increased replica count (2 replicas)
- Comprehensive testing (health, load, performance)
- Manual deployment option with custom image tags

### 5. Production Deployment (`deploy-production.yml`)
**Triggers**: Manual dispatch only
**Purpose**: Controlled production deployments

**Features**:
- **Manual approval required** (unless emergency override)
- Image validation and security scanning
- Blue-green deployment strategy
- Production configuration (3 replicas, resource limits)
- Comprehensive health checks with rollback capability
- Slack notifications for all deployment events

## Security Features

### Image Security
- Trivy security scanning on all builds
- SARIF upload to GitHub Security tab
- Production deployment blocked on CRITICAL/HIGH vulnerabilities
- Container registry authentication

### Access Control
- GitHub environments with protection rules
- Manual approval gates for production
- Separate Kubernetes configs per environment
- Secret isolation per environment

### Deployment Safety
- Image validation before deployment
- Health checks with automatic rollback
- Blue-green deployment for zero downtime
- Backup of previous deployment state

## Required Secrets

Set these secrets in your GitHub repository:

### Container Registry
- `GITHUB_TOKEN` (automatically provided)

### Kubernetes Access
- `KUBE_CONFIG_DEV` - Development cluster kubeconfig
- `KUBE_CONFIG_STAGING` - Staging cluster kubeconfig  
- `KUBE_CONFIG_PROD` - Production cluster kubeconfig

### Notifications
- `SLACK_WEBHOOK_URL` - Slack webhook for notifications

## GitHub Environments Setup

Create these environments in GitHub with protection rules:

### `development`
- No protection rules needed
- Auto-deployment allowed

### `staging`  
- No protection rules needed
- Auto-deployment allowed

### `production-approval`
- **Required reviewers**: Add team leads/DevOps team
- **Wait timer**: 5 minutes (optional)

### `production`
- **Required reviewers**: Add senior engineers  
- **Deployment branches**: Limit to `main` branch
- **Environment secrets**: Production kubeconfig

## Workflow Trigger Summary

| Action | Triggered Workflows |
|--------|-------------------|
| Push to `develop` | CI Build → Development Deployment |
| Push to `main` | CI Build → Staging Deployment |
| Push to `feature/*` | CI Build only |
| Push to `hotfix/*` | CI Build only |
| PR to `main/develop` | PR Build Validation only |
| Manual production deploy | Production Deployment |

## Deployment Process

### Feature Development
1. Create feature branch: `git checkout -b feature/my-feature`
2. Push changes - triggers **CI Build** only
3. Create PR to `develop` - triggers **PR Build Validation** only
4. Merge to `develop` - triggers **CI Build** → **Development Deployment**

### Staging Release
1. Create PR from `develop` to `main` - triggers **PR Build Validation**
2. Review and merge - triggers **CI Build** → **Staging Deployment**
3. Perform UAT in staging environment

### Production Release
1. Go to Actions → "Deploy to Production"
2. Click "Run workflow"
3. Enter the image tag (e.g., `main`, commit SHA, or `latest`)
4. Wait for approvals (unless emergency)
5. Monitor deployment and health checks

### Emergency Deployments
1. Use production workflow with `skip_approval: true`
2. Requires emergency deployment justification
3. Still includes all safety checks and validations

## Monitoring & Notifications

### Slack Notifications
- Development deployments: Success notifications
- Staging deployments: Success notifications  
- Production deployments: Success AND failure notifications
- All notifications include environment, image tag, and deployment details

### GitHub Summaries
Each workflow provides detailed summaries including:
- Build results and test status
- Security scan results
- Deployment details (namespace, replicas, image)
- Health check results

## Troubleshooting

### Common Issues

**Image not found errors**:
- Ensure CI build completed successfully
- Check image exists in GitHub Container Registry
- Verify image tag is correct

**Deployment fails**:
- Check Kubernetes cluster connectivity
- Verify namespace and RBAC permissions
- Review pod logs: `kubectl logs -n <namespace> <pod-name>`

**Health checks fail**:
- Verify application starts correctly
- Check service and ingress configuration
- Review load balancer setup

### Rollback Procedure
1. Go to "Deploy to Production" workflow
2. Use a previous known-good image tag
3. Follow normal deployment process
4. Production workflow includes automatic rollback on health check failures

## Migration from Old Workflow

The old `docker-build-deploy.yml` has been deprecated and disabled to prevent accidental deployments. It can only be triggered manually in emergencies with the `force_deploy` parameter.

To fully migrate:
1. Test the new workflows in development
2. Verify staging deployments work correctly  
3. Perform initial production deployment
4. Remove or archive the old workflow file