# Fed Learn Server Aggregator

## Local Development

### Build the image

```bash
docker build -t fl-server-aggregate-test -f Dockerfile .
```

### Run the container

```bash
# add -d flag to run in detached mode if needed
# add it before the image name
docker run -it --name fl-server-aggregate-test-container -p 9001:9001 fl-server-aggregate-test
```
