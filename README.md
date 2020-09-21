# ProgImage

## Preface

This project is written in Java and uses [Micronaut](https://micronaut.io) micro-framework. I decided to use Micronaut
over Spring Boot for a few reasons:

1) Micronaut memory footprint is much lower than Spring Boot
2) Startup time is also significant faster
3) Micronaut build on top of Netty, which is a reactive non-blocking HTTP server

## Prerequisites

### OpenJDK

This demo project requires installed OpenJDK 11 or newer. Please use one of the following tools to install OpenJDK:

1) https://sdkman.io
2) https://asdf-vm.com

### kubectl

Install `kubectl` to manage local Kubernetes cluster: https://kubernetes.io/docs/tasks/tools/install-kubectl/

### minikube

Please follow this instruction to install `minikube`: https://kubernetes.io/docs/tasks/tools/install-minikube/

### k6

This project is using `k6` for performance testing, please follow this manual to check how to install `k6`:
https://k6.io/docs/getting-started/installation

## How to run this project?

### Run tests

Each microservice from this project contains a suite of tests. To run all tests you need to execute such command:

```bash
./gradlew test
```

### Build Docker images

Run the following command to build Docker images for all microservices, includes `storage`, `compression`, `rotation`,
`filters`, `thumbnail`:

```bash
./gradlew dockerBuild
```

### Deploy containers to Kubernetes

Create a local Kubernetes cluster with `minikube`:

```bash
minikube start --vm=true
```

Wait after node will change the status to `Ready`:

```bash
kubectl get no
```

Enable Docker registry and nginx-ingress:

```bash
minikube addons enable ingress
minikube addons enable registry
```

Forward registry port to localhost:

```bash
docker run --rm -it -d --network=host alpine ash -c "apk add socat && socat TCP-LISTEN:5000,reuseaddr,fork TCP:$(minikube ip):5000"
```

Push Docker images to the local registry:

```bash
docker tag progimage-storage:latest localhost:5000/progimage-storage:latest
docker push localhost:5000/progimage-storage:latest

docker tag progimage-compression:latest localhost:5000/progimage-compression:latest
docker push localhost:5000/progimage-compression:latest

docker tag progimage-rotation:latest localhost:5000/progimage-rotation:latest
docker push localhost:5000/progimage-rotation:latest

docker tag progimage-filters:latest localhost:5000/progimage-filters:latest
docker push localhost:5000/progimage-filters:latest

docker tag progimage-thumbnail:latest localhost:5000/progimage-thumbnail:latest
docker push localhost:5000/progimage-thumbnail:latest
```

Deploy applications to Kubernetes:

```bash
kubectl apply -f kubernetes/.
```

Wait until all pods are ready:

```bash
kubectl get po
```

Try to upload the image:

```bash
curl http://$(minikube ip)/storage/upload \
-F "file=@$(pwd)/progimage-storage/src/test/resources/testphoto1.jpeg" -v
```
