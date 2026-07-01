import grpc
import gateway_pb2
import gateway_pb2_grpc
from oauthinterceptor import OAuthInterceptor

def run():

    # OAuth Interceptor Configuration
    token_url = "http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token"
    client_id = "zeebe"
    client_secret = "NFp6GKwftJ"
    audience = "zeebe-api"

     # Create an instance of the OAuthInterceptor
    oauth_interceptor = OAuthInterceptor(token_url, client_id, client_secret, audience)

    # Add interceptor to the channel
    intercept_channel = grpc.intercept_channel(
        grpc.insecure_channel('localhost:26500'), oauth_interceptor)

    # Now use the intercepted channel to create stubs
    stub = gateway_pb2_grpc.GatewayStub(intercept_channel)

    topologyResponse = stub.Topology(gateway_pb2.TopologyRequest())
    print(topologyResponse)

    tenantIds = ['custom'] # tenantIds
    fileName = "example.bpmn"
    with open(fileName, 'rb') as file:
        bpmn_content = file.read()
        print(bpmn_content)
        # Deploy Diagram
        for tenantId in tenantIds:
            resource = gateway_pb2.Resource(name=fileName, content=bpmn_content)
            deployResult = stub.DeployResource(gateway_pb2.DeployResourceRequest(tenantId=tenantId , resources=[resource]))
            print(deployResult)

    # Start Instances
    for tenantId in tenantIds:
        for x in range(20):
            stub.CreateProcessInstance(gateway_pb2.CreateProcessInstanceRequest(tenantId = tenantId, bpmnProcessId="example", version=-1 ))

    # Job worker logic (Activate and complete jobs)
    job_type = 'dummy'
    worker = 'python-worker'
    timeout = 10000  # in milliseconds
    maxJobsToActivate=10
    request = gateway_pb2.ActivateJobsRequest(type=job_type, worker=worker, timeout=timeout, maxJobsToActivate=maxJobsToActivate, tenantIds=tenantIds)

    while True:
        for activate_response in stub.ActivateJobs(request):
            for job in activate_response.jobs:
                print(f"Activated job {job.key}")
                # Process the job here

                # Complete the job
                complete_request = gateway_pb2.CompleteJobRequest(jobKey=job.key, variables='{}')
                stub.CompleteJob(complete_request)
                print(f"Completed job {job.key}")
        print("Looking for jobs again...")



if __name__ == '__main__':
    run()