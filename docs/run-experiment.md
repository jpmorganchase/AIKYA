# AIKYA Federated Learning - Experiment Guide

Learn how to run your first federated learning experiment using AIKYA's fraud detection capabilities. This guide walks you through conducting experiments with multiple datasets and federated learning algorithms.

## Table of Contents

- [Table of Contents](#table-of-contents)
- [Prerequisites](#prerequisites)
- [Understanding the Experiment](#understanding-the-experiment)
  - [What You'll Accomplish](#what-youll-accomplish)
  - [Experiment Architecture](#experiment-architecture)
  - [Available Datasets](#available-datasets)
    - [**Payment Fraud Detection Datasets**](#payment-fraud-detection-datasets)
- [Pre-Experiment Setup](#pre-experiment-setup)
  - [Verify Services](#verify-services)
  - [Access the Dashboards](#access-the-dashboards)
- [Running Your First Experiment](#running-your-first-experiment)
  - [Step 1: Load Data into Banks](#step-1-load-data-into-banks)
    - [Load Data into Bank 1](#load-data-into-bank-1)
    - [Load Data into Bank 2](#load-data-into-bank-2)
  - [Step 2: Observe Individual Performance](#step-2-observe-individual-performance)
    - [Bank 1 Analysis](#bank-1-analysis)
    - [Bank 2 Analysis](#bank-2-analysis)
  - [Step 3: Start Federated Learning](#step-3-start-federated-learning)
    - [Initiate Federated Training](#initiate-federated-training)
    - [What Happens During Federated Learning](#what-happens-during-federated-learning)
  - [Step 4: Monitor Training Progress](#step-4-monitor-training-progress)
    - [Client Dashboard Monitoring](#client-dashboard-monitoring)
  - [Step 5: Analyze Results](#step-5-analyze-results)
    - [Performance Comparison](#performance-comparison)
    - [Expected Improvements](#expected-improvements)
- [Advanced Experiments](#advanced-experiments)
  - [Multi-Round Training](#multi-round-training)
- [Monitoring and Debugging](#monitoring-and-debugging)
  - [Container Log Analysis](#container-log-analysis)
  - [Performance Metrics](#performance-metrics)
- [Troubleshooting](#troubleshooting)
  - [Common Issues](#common-issues)
    - [**🔄 Training Stuck or Not Starting**](#-training-stuck-or-not-starting)
    - [**📊 No Data Visible After Loading**](#-no-data-visible-after-loading)
    - [**🔌 UI Not Responding**](#-ui-not-responding)
    - [**⚠️ Aggregation Fails**](#️-aggregation-fails)
  - [Getting Help](#getting-help)

## Prerequisites

Before running experiments, ensure you have completed the [Quickstart Guide](./quickstart-guide.md) and have:

✅ **All AIKYA services running**

- Server components (orchestrator, aggregator, database, UI)
- Bank 1 client components (all services operational)
- Bank 2 client components (all services operational)

✅ **Network connectivity verified**

- All containers can communicate with each other
- Web interfaces are accessible

✅ **Basic understanding of federated learning concepts**

- Local training vs. global aggregation
- Privacy-preserving machine learning principles

## Understanding the Experiment

### What You'll Accomplish

In this experiment, you will:

1. **Load transaction data** into two simulated banks (Bank 1 and Bank 2)
2. **Observe individual fraud detection** performance using each bank's isolated model
3. **Conduct federated learning** to create a shared model without exposing raw data
4. **Compare performance improvements** achieved through collaborative learning
5. **Analyze the federated learning process** and understand its benefits

### Experiment Architecture

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    Bank 1   │    │   Server    │    │    Bank 2   │
│             │    │             │    │             │
│ Local Data  │◄──►│ FL Orchestr.│◄──►│ Local Data  │
│ Local Model │    │ FL Aggreg.  │    │ Local Model │
│             │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘
       │                  │                  │
       ▼                  ▼                  ▼
   Individual        Global Model       Individual
   Performance      Aggregation        Performance
```

### Available Datasets

AIKYA provides several pre-configured datasets for experimentation:

#### **Payment Fraud Detection Datasets**

Located in: `/clien/seeds/payment_fraud/`

**Dataset Types:**

- **No Fraud Datasets**: Clean transaction data without fraudulent patterns
  - `bank1_[no_fraud]_[app_frac_1]_[no_overlap]_[1-2].csv`
  - `bank2_[no_fraud]_[app_frac_1]_[no_overlap]_[1-2].csv`

- **Type 1 Fraud Patterns**: Moderate complexity fraud scenarios
  - Evaluation sets: `bank*_[type1]_[app_frac_0.9]_[no_overlap]_[eval_1-5].csv`
  - Training sets: `bank*_[type1]_[app_frac_0.9]_[no_overlap]_[gen_train_1-2].csv`
  - Scaling sets: `bank*_[type1]_[app_frac_0.9]_[no_overlap]_[scaling_1].csv`

- **Type 2 Fraud Patterns**: Complex fraud scenarios with advanced patterns
  - Evaluation sets: `bank*_[type2]_[app_frac_0.9]_[no_overlap]_[eval_1-5].csv`
  - Training sets: `bank*_[type2]_[app_frac_0.9]_[no_overlap]_[gen_train_1-2].csv`
  - Scaling sets: `bank*_[type2]_[app_frac_0.9]_[no_overlap]_[scaling_1].csv`

**Additional Dataset Categories:**

- **Credit Card Fraud**: `/client/seeds/credit_card_fraud/`
- **Payment Processing**: `/client/seeds/payment/`

## Pre-Experiment Setup

### Verify Services

Before starting your experiment, verify all services are healthy:

```bash
# Check container health status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep aikya

# Test web interface connectivity
curl -s http://localhost:3000 > /dev/null && echo "Bank 1 UI: ✅" || echo "Bank 1 UI: ❌"
curl -s http://localhost:3001 > /dev/null && echo "Bank 2 UI: ✅" || echo "Bank 2 UI: ❌"
curl -s http://localhost:4000 > /dev/null && echo "Server UI: ✅" || echo "Server UI: ❌"
```

### Access the Dashboards

Open the following URLs in separate browser tabs:

| Dashboard | URL | Purpose |
|-----------|-----|---------|
| **Bank 1 (JPM Simulation)** | <http://localhost:3000> | Manage Bank 1 data and local training |
| **Bank 2 (Citi Simulation)** | <http://localhost:3001> | Manage Bank 2 data and local training |
| **Server Dashboard** | <http://localhost:4000> | Monitor global federated learning process |

**Login Credentials** (for all dashboards):

- **Username**: `admin@admin.com`
- **Password**: `admin`

## Running Your First Experiment

### Step 1: Load Data into Banks

#### Load Data into Bank 1

1. Navigate to Bank 1 dashboard (<http://localhost:3000>)
2. Log in with the provided credentials
3. Click the **"Load Transactions"** button (top right corner)
4. Select a dataset from the file picker:
   - **Recommended for first experiment**: `bank1_[type1]_[app_frac_0.9]_[no_overlap]_[eval_1].csv`
5. Wait for the data loading process to complete
6. Verify that transactions appear in the dashboard

#### Load Data into Bank 2

1. Navigate to Bank 2 dashboard (<http://localhost:3001>)
2. Log in with the provided credentials
3. Click the **"Load Transactions"** button (top right corner)
4. Select a corresponding dataset:
   - **Recommended**: `bank2_[type1]_[app_frac_0.9]_[no_overlap]_[eval_1].csv`
5. Wait for the data loading process to complete
6. Verify that transactions appear in the dashboard

### Step 2: Observe Individual Performance

After loading data, observe how each bank performs fraud detection independently:

#### Bank 1 Analysis

1. Review the transaction summary and fraud detection statistics
2. Note the baseline accuracy, precision, and recall metrics
3. Examine false positive and false negative rates
4. Observe which types of transactions are flagged as potentially fraudulent

#### Bank 2 Analysis

1. Compare Bank 2's metrics with Bank 1's performance
2. Note differences in detection patterns between the banks
3. Document baseline performance for later comparison

**Key Metrics to Record:**

- Total transactions processed
- Fraud detection accuracy
- False positive rate
- False negative rate
- Processing time

### Step 3: Start Federated Learning

#### Initiate Federated Training

1. **On either Bank 1 or Bank 2 dashboard:**
   - Locate the **"Batch Status View"** section
   - Click the **"Share Insights"** button

2. **⚠️ Important Guidelines:**
   - Click "Share Insights" **only once** per training round
   - Multiple clicks may cause training conflicts
   - Wait for the current round to complete before starting another

#### What Happens During Federated Learning

```text
1. Local Training Phase:
   ┌─────────┐                           ┌─────────┐
   │ Bank 1  │ Trains model on local data │ Bank 2  │
   │         │                           │         │
   └─────────┘                           └─────────┘
        │                                     │
        ▼                                     ▼
   [Model Updates]                      [Model Updates]

2. Aggregation Phase:
   [Model Updates] ────► [Server Aggregator] ◄──── [Model Updates]
                             │
                             ▼
                      [Global Model]

3. Distribution Phase:
   [Global Model] ──────► Bank 1 & Bank 2 ◄────── [Global Model]
```

### Step 4: Monitor Training Progress

#### Client Dashboard Monitoring

**On Bank Dashboards:**

- **Training Status**: Local model training progress
- **Communication Status**: Connection with the federated learning server
- **Model Version**: Current global model version received
- **Performance Updates**: Real-time accuracy improvements

### Step 5: Analyze Results

#### Performance Comparison

After the federated learning round completes:

1. **Return to Bank 1 Dashboard**
   - Compare new fraud detection metrics with baseline
   - Note improvements in accuracy, precision, and recall
   - Observe changes in false positive/negative rates

2. **Return to Bank 2 Dashboard**
   - Perform the same comparison analysis
   - Document performance improvements

3. **Server Dashboard Analysis**
   - Review global model performance metrics
   - Examine aggregation statistics and convergence patterns

#### Expected Improvements

Typically, you should observe:

- **Increased Detection Accuracy**: Better identification of fraudulent transactions
- **Reduced False Positives**: Fewer legitimate transactions flagged as fraud
- **Enhanced Pattern Recognition**: Improved detection of sophisticated fraud schemes
- **Balanced Performance**: More consistent performance across both banks

## Advanced Experiments

### Multi-Round Training

Conduct multiple federated learning rounds to observe continuous improvement:

1. **Complete Round 1** using the basic experiment workflow
2. **Start Round 2** by clicking "Share Insights" again
3. **Monitor Progressive Improvement** across multiple rounds
4. **Track Convergence** - performance improvements may diminish over rounds

**Recommended Rounds**: 3-5 rounds for optimal observation of federated learning benefits

## Monitoring and Debugging

### Container Log Analysis

Monitor federated learning progress through container logs:

```bash
# Monitor server orchestrator logs
docker logs -f aikya-fl-server-server-orchestrator-1

# Monitor server aggregator logs  
docker logs -f aikya-fl-server-server-fl-aggregator-1

# Monitor client orchestrator logs
docker logs -f aikya-client-bank1-client-orchestrator-1
docker logs -f aikya-client-bank2-client-orchestrator-1

# Monitor client agent logs
docker logs -f aikya-client-bank1-client-agent-1
docker logs -f aikya-client-bank2-client-agent-1
```

### Performance Metrics

Track key performance indicators:

**Model Performance:**

- Training loss convergence
- Validation accuracy trends
- Fraud detection precision/recall

**System Performance:**

- Training round completion time
- Network communication latency
- Resource utilization (CPU/Memory)

**Federation Metrics:**

- Number of participating clients
- Model update transmission success rate
- Aggregation algorithm convergence


## Troubleshooting

### Common Issues

#### **🔄 Training Stuck or Not Starting**

```bash
# Check if all required services are running
docker ps | grep aikya

# Verify client-server connectivity
docker logs aikya-fl-server-server-orchestrator-1 | tail -20

# Restart federated learning components if needed
docker restart aikya-fl-server-server-orchestrator-1
docker restart aikya-fl-server-server-fl-aggregator-1
```

#### **📊 No Data Visible After Loading**

- Verify dataset file format and structure
- Check client data processor logs for errors
- Ensure sufficient disk space for data processing

#### **🔌 UI Not Responding**

```bash
# Check UI container status
docker logs aikya-client-bank1-client-ui-1
docker logs aikya-client-bank2-client-ui-1

# Restart UI containers if needed
docker restart aikya-client-bank1-client-ui-1
docker restart aikya-client-bank2-client-ui-1
```

#### **⚠️ Aggregation Fails**

- Ensure both banks have loaded data before starting aggregation
- Check server aggregator logs for specific error messages
- Verify network connectivity between all components
- Confirm sufficient system resources (CPU/Memory)

### Getting Help

If you encounter persistent issues:

1. **Check Service Health**: Verify all containers are running and healthy
2. **Review Logs**: Examine container logs for specific error messages  
3. **Resource Check**: Ensure sufficient system resources
4. **Network Verification**: Test connectivity between all components
5. **Clean Restart**: Stop all services and restart following the quickstart guide

For additional support, consult the project documentation or reach out to the development team with:

- Detailed error messages from logs
- Steps to reproduce the issue
- System specifications and environment details
