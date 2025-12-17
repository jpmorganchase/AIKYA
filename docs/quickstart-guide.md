# AIKYA Federated Learning - Quickstart Guide
**⚠️ Aikya is a Proof of Concept and not meant for production usage**

 This guide will help you set up and run your first federated learning experiment with simulated bank clients.

## Table of Contents

- [Table of Contents](#table-of-contents)
- [What You'll Learn](#what-youll-learn)
- [Architecture Overview](#architecture-overview)
  - [Server Components](#server-components)
  - [Client Components (Per Bank)](#client-components-per-bank)
- [Prerequisites](#prerequisites)
  - [System Requirements](#system-requirements)
  - [Software Requirements](#software-requirements)
- [Quick Setup](#quick-setup)
  - [1. Prepare Your Environment](#1-prepare-your-environment)
    - [⚠️ IMPORTANT: Clean Docker Environment](#️-important-clean-docker-environment)
  - [2. Start the Federated Learning Server](#2-start-the-federated-learning-server)
  - [3. Start the Client Banks](#3-start-the-client-banks)
  - [4. Verify Your Setup](#4-verify-your-setup)
- [Understanding the Components](#understanding-the-components)
  - [Server Component Details](#server-component-details)
  - [Client Components](#client-components)
    - [Bank 1](#bank-1)
    - [Bank 2](#bank-2)
- [Accessing the Platform](#accessing-the-platform)
  - [🖥️ Web Interfaces](#️-web-interfaces)
  - [🔧 API Endpoints](#-api-endpoints)
- [Next Steps](#next-steps)
- [Troubleshooting](#troubleshooting)
  - [Common Issues](#common-issues)
    - [🐳 Docker Issues](#-docker-issues)
  - [Getting Help](#getting-help)
  - [Stopping the Environment](#stopping-the-environment)

## What You'll Learn

By following this guide, you will:

- Set up a complete federated learning environment with 1 server and 2 simulated bank clients
- Understand the key components of the AIKYA platform
- Learn how to access and interact with the federated learning system
- Be ready to run your first federated learning experiments

## Architecture Overview

AIKYA implements a federated learning architecture with the following key components:

### Server Components

- **FL Server Orchestrator**: Coordinates the federated learning process
- **FL Server Aggregator**: Combines model updates from clients using algorithms like FedAvg
- **FL Server Database**: Stores orchestration data and aggregation results
- **FL Server UI**: Web interface for monitoring and controlling federated learning experiments

### Client Components (Per Bank)

- **Client Orchestrator**: Manages local federated learning operations
- **Client Agent**: Handles model training and local computations
- **Client Data Processor**: Manages data preparation and feature engineering
- **Client Database**: Stores local training data and model artifacts
- **Client UI**: Bank-specific interface for data management and monitoring

## Prerequisites

### System Requirements

- **CPU**: Minimum quad-core processor (ARM or x64 architecture)
- **Memory**: Minimum 8GB RAM (16GB recommended for better performance)
- **Storage**: At least 10GB free disk space
- **Operating System**:
  - macOS with Zsh terminal
  - Linux with Bash terminal
  - Windows with WSL2 or Git Bash

### Software Requirements

1. **Docker Desktop**
   - Although it is strongly recommended that you use a Docker Desktop version released in the last 6 months, ensure that you have a minimum `docker compose` version of `2.17.0` or higher and a `docker` API version of at least `1.42`. You can check the docker version by running:
     ```bash
     # find docker compose version
     docker compose version

     # find docker version
     docker version --format "Client API Version: {{.Client.APIVersion}} || Server Version: {{.Server.Version}}"
     ```
      - If you are running an older version, please update Docker Desktop to the latest version from Docker's official website.
        - [macOS](https://docs.docker.com/desktop/install/mac-install/).
          - Note: If you are using homebrew, you can install/update Docker Desktop with:
            ```bash
            brew install --cask docker-desktop
            ```
        - [Windows](https://docs.docker.com/desktop/install/windows-install/)
        - [Linux](https://docs.docker.com/desktop/setup/install/linux/)
   - The instructions here should work with Docker Desktop version `4.44.1` and up.
   

2. **Terminal Access**
   - macOS: Built-in Terminal with Zsh
   - Linux: Bash terminal
   - Windows: WSL2 or Git Bash

## Quick Setup

### 1. Prepare Your Environment

> Ensure Docker is running before proceeding. You can verify this by running: `docker run hello-world`

To get started, clone the AIKYA repository and navigate to the build directory:

```bash
# First clone the AIKYA repository if you haven't already
git clone https://github.com/onyx-incubator/aikya-oss.git

# Navigate to the AIKYA build directory
cd aikya-oss/build-and-run

# OPTIONAL: Set Docker BuildKit progress to plain for cleaner output
export BUILDKIT_PROGRESS=plain
```

#### ⚠️ IMPORTANT: Clean Docker Environment

For the best experience, start with a clean Docker environment:

> CAUTION: This will remove ALL Docker resources on your machine
> Skip this step if you have other important Docker containers/images

```bash
docker volume prune -af \
&& docker network prune -f \
&& docker system prune -af
```

### 2. Start the Federated Learning Server

Launch the server components that will coordinate the federated learning process:

```bash
# Build and start the FL server (orchestrator, aggregator, database, UI)
docker compose -f compose-server.yml --env-file env-vars/server.vars.env up -d --build
```

This command will:

- Build Docker images for all server components
- Start the server orchestrator (port `9000`)
- Start the server aggregator (port `9001`)
- Start the server database (port `3310`)
- Start the server web UI (port `4000`)

### 3. Start the Client Banks

Launch two simulated bank clients that will participate in federated learning:

```bash
# Build and start Bank 1 client components
docker compose -f compose-client.yml --env-file env-vars/bank1.vars.env up -d --build

# Build and start Bank 2 client components  
docker compose -f compose-client.yml --env-file env-vars/bank2.vars.env up -d --build
```

Each bank client includes:

- Client orchestrator (Bank1: port `8080`, Bank2: port `8081`)
- Client agent (Bank1: port `17000`, Bank2: port `17002`)
- Client data processor (Bank1: port `17001`, Bank2: port `17003`)
- Client database (Bank1: port `3306`, Bank2: port `3307`)
- Client web UI (Bank1: port `3000`, Bank2: port `3001`)

### 4. Verify Your Setup

Check that all containers are running successfully:

```bash
# List all running AIKYA containers with detailed information
docker container ls --no-trunc -f "name=aikya" \
  --format "=== Container: '{{.Names}}' ===\n\n \
    ID: '{{.ID}}'\n \
    Image: '{{.Image}}'\n \
    Status: '{{.Status}}'\n \
    Ports: '{{.Ports}}'\n \
    Networks: '{{.Networks}}'\n"
```

You should see approximately 11 containers running:

- 4 server containers (aikya-fl-server-*)
- 7 client containers (aikya-client-bank1-*, aikya-client-bank2-*)

## Understanding the Components

### Server Component Details

| Component               | Purpose                                   | Port | Access URL              |
| ----------------------- | ----------------------------------------- | ---- | ----------------------- |
| **Server Orchestrator** | Coordinates FL process across clients     | 9000 | <http://localhost:9000> |
| **Server Aggregator**   | Performs model aggregation (FedAvg, etc.) | 9001 | Internal Only           |
| **Server Database**     | Stores orchestration and aggregation data | 3310 | Internal only           |
| **Server UI**           | Web dashboard for monitoring experiments  | 4000 | <http://localhost:4000> |

### Client Components

#### Bank 1

| Component                 | Purpose                     | Port  | Access URL              |
| ------------------------- | --------------------------- | ----- | ----------------------- |
| **Client Orchestrator**   | Manages local FL operations | 8080  | <http://localhost:8080> |
| **Client Agent**          | Handles model training      | 17000 | Internal API            |
| **Client Data Processor** | Manages data preparation    | 17001 | Internal API            |
| **Client Database**       | Stores local training data  | 3306  | Internal only           |
| **Client UI**             | Bank 1 web interface        | 3000  | <http://localhost:3000> |

#### Bank 2

| Component                 | Purpose                     | Port  | Access URL              |
| ------------------------- | --------------------------- | ----- | ----------------------- |
| **Client Orchestrator**   | Manages local FL operations | 8081  | <http://localhost:8081> |
| **Client Agent**          | Handles model training      | 17002 | Internal API            |
| **Client Data Processor** | Manages data preparation    | 17003 | Internal API            |
| **Client Database**       | Stores local training data  | 3307  | Internal only           |
| **Client UI**             | Bank 2 web interface        | 3001  | <http://localhost:3001> |

## Accessing the Platform

Once all containers are running, you can access the different interfaces:

### 🖥️ Web Interfaces

1. **Server Dashboard** (Experiment Management)
   - URL: <http://localhost:4000>
   - Use this to: Monitor global experiments, view aggregation results, manage federated learning rounds

2. **Bank 1 Dashboard**
   - URL: <http://localhost:3000>
   - Use this to: Manage Bank 1's local data, monitor training progress, view local model performance

3. **Bank 2 Dashboard**
   - URL: <http://localhost:3001>
   - Use this to: Manage Bank 2's local data, monitor training progress, view local model performance

### 🔧 API Endpoints

- **Server Orchestrator API**: <http://localhost:9000/orchestrator-server/api>
- **Server Aggregator API**: <http://localhost:9001/aggregate/api>
- **Bank 1 Orchestrator API**: <http://localhost:8080/orchestrator-cli/api>
- **Bank 2 Orchestrator API**: <http://localhost:8081/orchestrator-cli/api>

## Next Steps

Now that your AIKYA federated learning environment is running, you can:

1. **Load Sample Data**: Use the client UIs to load sample datasets for credit card fraud detection or payment fraud
2. **Configure Experiments**: Set up federated learning experiments with different algorithms (FedAvg, FedProx, etc.)
3. **Run Training**: Start federated learning rounds and monitor the training progress
4. **Analyze Results**: Review model performance and aggregation results through the web dashboards

For detailed experiment instructions, see the [run-experiment.md](run-experiment.md) guide.

## Troubleshooting

### Common Issues

#### 🐳 Docker Issues

```bash
# If containers fail to start, check Docker logs
docker compose -f compose-server.yml logs

# For specific container logs
docker logs <container-name>
```

**🔌 Port Conflicts**
If you get port binding errors, ensure no other services are using the required ports:

```bash
# Check what's using specific ports
lsof -i :3000  # or other port numbers
netstat -tulpn | grep :3000
```

**💾 Memory Issues**
If builds fail due to memory constraints:

- Close other applications to free up RAM
- Increase Docker Desktop memory allocation (Settings > Resources > Memory)
- Consider building components one at a time instead of all at once

**🔄 Build Issues**
If Docker builds fail:

```bash
# Try rebuilding without cache
docker compose -f compose-server.yml build --no-cache
```

**🌐 Network Issues**
If services can't communicate:

```bash
# Verify Docker networks
docker network ls
docker network inspect aikya-fl-server-net
```

### Getting Help

If you encounter issues:

1. Check the container logs for specific error messages
2. Ensure all prerequisites are installed correctly
3. Verify Docker Desktop is running and has sufficient resources
4. Consult the project documentation for advanced configuration options

### Stopping the Environment

To stop all AIKYA services:

```bash
# Stop all services gracefully
docker compose -f compose-server.yml down
docker compose -f compose-client.yml --env-file env-vars/bank1.vars.env down  
docker compose -f compose-client.yml --env-file env-vars/bank2.vars.env down
```
