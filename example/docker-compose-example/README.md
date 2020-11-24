# Java spring service example

## Usage

### Prerequisites

1. Docker compose
2. This example assumes the Demo tenant in the demo-env, Java Sprint Demo project and the Spring app inside

### Setup

Run the following:
```
docker-compose build
```

Update the `docker-compose.yml` file to include the relevant env-vars (API_KEY, API_SECRET, CONTROL_PLANE_ADDR)

### Run
```
docker-compose up
```
Two endpoints available:
1. http://localhost:8080/sdk - get the actual result from the PDP
2. http://localhost:8080/websecurity - get the result of the /allow from the policy - Forbidden - 403

### Services
This example uses 2 services:
1. Java Spring - entry point - port 8080
2. pdp - port 8181
