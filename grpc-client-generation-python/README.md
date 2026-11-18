## Client Generation with Protocol Buffers

gRPC is a high-performance, open-source universal RPC (Remote Procedure Call) framework that uses protocol buffers as its interface definition language. One of its powerful features is the automatic generation of client and server code from .proto files, which define the service methods and message types. This process simplifies the development of gRPC services and clients, making it easier to build distributed applications and microservices. Here's how gRPC allows client (and server) code generation based on .proto files:

## 1.  Retrieve Service and Messages in .proto File

For 8.4 the .proto file can be retrieved here:

```
curl -O https://raw.githubusercontent.com/camunda/zeebe/stable/8.4/gateway-protocol/src/main/proto/gateway.proto
```

## 2. Install Required Tools
To generate Python code, you need the Protocol Buffer compiler (protoc) and the Python gRPC plugin. If you haven't installed these, you can do so as follows:

Install protoc from the [official releases](https://grpc.io/docs/protoc-installation/) page or via a package manager for your system.

```
apt install -y protobuf-compiler
```

Install the Python gRPC tools using pip:

```
pip install grpcio-tools
```

## 3. Generate Python gRPC Code

```
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. gateway.proto
```

After running this command, you should see two new files in your directory:

- `gateway_pb2.py`: Contains the generated request and response classes.
- `gateway_pb2_grpc.py`: Contains the generated client and server classes.


### 4. Implement OAuth Interceptor

The interceptor is required to seamlessly inject authentication tokens into all outgoing gRPC requests, ensuring secure communication with the Zeebe broker without manually adding tokens to each call. It works by intercepting each call, obtaining a fresh OAuth token if necessary, and appending it to the request's metadata as an Authorization header.

Example Implementation [here](oauthinterceptor.py).

### 5. Write Zeebe Client
Example Implementation [here](zeebe_client.py).



