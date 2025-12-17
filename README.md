# Aikya

AIKYA is a proof-of-concept project that leverages federated learning frameworks(FL) for enhancing anomaly detection in financial transactions. The project evaluates whether decentralized model training across multiple institutions can achieve comparable performance to centralized training—without sharing raw data.

This repository is intended for engineers, researchers, and technically inclined practitioners exploring federated learning architectures.

## Table of Contents <!-- omit in toc -->

- [System Architecture](#system-architecture)
- [Build and run](#build-and-run)
- [Sequence flow](#sequence-flow)
- [Data](#data)
- [Citation](#citation)
- [Notice](#notice)
- [Notes and Contribution](#notes-and-contribution)
- [Disclaimer](#disclaimer)
- [License](#license)
- [Frequently Asked Questions (FAQ)](#frequently-asked-questions-faq)
  - [What is AIKYA?](#what-is-aikya)
  - [Is this a production-ready system?](#is-this-a-production-ready-system)
  - [What problem does this project address?](#what-problem-does-this-project-address)
  - [What type of data does AIKYA use?](#what-type-of-data-does-aikya-use)
  - [Does AIKYA provide privacy or security guarantees?](#does-aikya-provide-privacy-or-security-guarantees)
  - [How can I engage with the project?](#how-can-i-engage-with-the-project)
  - [Are performance results representative of real-world systems?](#are-performance-results-representative-of-real-world-systems)
  - [Is this project related to a publication?](#is-this-project-related-to-a-publication)

## System Architecture

The experimental FL setup employs a client-server architecture. The below figure provides a high-level overview of the system architecture and components.

![System Architecture](./docs/resources/Aikya-Network-Diagram.png)

## Build and run

**⚠️ Aikya is a Proof of Concept and not meant for production usage**

- To build and run services locally, please refer to the [quickstart guide](./docs/quickstart-guide.md).
- To understand the experimental process and steps, please refer to [run instructions](./docs/run-experiment.md).

## Sequence flow

All the experiments are entirely driven via the project's custom user interface, ensuring reproducibility and traceability. Prior to interaction, synthetically generated datasets are loaded onto participant nodes to ingest and reference in the database. Participant UIs are accessible via unique URIs for each participant, with participant systems physically isolated on distinct machines. All subsequent steps assume network bootstrapping is complete.

![Sequence Flow](./docs/resources/Aikya-Workflow-Diagram.png)

## Data

All the data is synthetically generated using open-source tools such as `faker`. The generated data is available under the [seeds](./client/seeds) directory.

## Citation

J.P. Morgan and BNY (2025). *Project AIKYA: Enhanced Anomaly Detection in Financial Transactions through Decentralized AI*. https://www.jpmorgan.com/kinexys/content-hub/project-aikya

## Notice

Please refer to the [NOTICE](./NOTICE) file for more details on third party software used as part of this repository.

## Notes and Contribution

- This repository is currently a static artifact of our research. To maintain alignment with the published results, we are not accepting Pull Requests or active issue tracking at this time. However, feel free to fork the repository for your own work.
- The codebase is provided as-is. It does not account for all edge cases and is not supported through external bug-fix requests. Issues and fixes are addressed internally at the discretion of the maintainers.
- Code quality and performance are not optimized and are out of scope for the current release. Improvements, if any, will be introduced only in future versions.
- This repository must not be used as a reference for production systems. It exists solely to generate results in evaluating FL presented in the Decentralized AI Kinexys industry paper and is provided without warranties or guarantees of any kind.

## Disclaimer

This project is provided for research and educational purposes only. It should not be used as-is in production systems or relied upon for compliance-sensitive workflows.

## License

AIKYA is licensed under Apache 2.0 license. Please refer to [LICENSE](./LICENSE) file.

---

## Frequently Asked Questions (FAQ)

### What is AIKYA?

AIKYA is an experimental, research-oriented implementation of federated learning for anomaly detection in financial systems. It demonstrates how multiple participants can collaboratively train machine learning models without sharing raw data.

### Is this a production-ready system?

No. AIKYA is not production-ready. It has not been designed, tested, or validated for production use and should not be used in operational, commercial, or compliance-sensitive environments.

### What problem does this project address?

The project explores whether federated learning can achieve comparable outcomes to centralized training for anomaly detection while respecting data locality and privacy.

### What type of data does AIKYA use?

The repository uses synthetic or simulated datasets for experimentation. No real customer or financial data is included.

### Does AIKYA provide privacy or security guarantees?

No. AIKYA demonstrates federated learning mechanics only. It does not implement or guarantee:

- Secure aggregation
- Differential privacy
- Cryptographic protections
- Adversarial robustness

### How can I engage with the project?

Users are encouraged to Review the code, Reproduce experiments, and Open issues for questions, clarifications, or research discussion.

### Are performance results representative of real-world systems?

No. Experimental results are directional and illustrative. They are intended to support research exploration rather than serve as benchmarks for real-world deployments.

### Is this project related to a publication?

Yes. The repository supports experimentation and validation of concepts discussed in the Decentralized AI Kinexys industry paper. You can find the publication under [Citation](#citation) section.
