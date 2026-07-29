from collections import namedtuple
from grpc import UnaryUnaryClientInterceptor, StreamStreamClientInterceptor, UnaryStreamClientInterceptor, StreamUnaryClientInterceptor
import requests
import time

# Maintain the namedtuple definition for ClientCallDetails
ClientCallDetails = namedtuple('ClientCallDetails', ('method', 'timeout', 'metadata', 'credentials', 'wait_for_ready', 'compression'))

class OAuthInterceptor(UnaryUnaryClientInterceptor, UnaryStreamClientInterceptor, StreamUnaryClientInterceptor, StreamStreamClientInterceptor):
    def __init__(self, token_url, client_id, client_secret, audience):
        self.token_url = token_url
        self.client_id = client_id
        self.client_secret = client_secret
        self.audience = audience
        self.token = None
        self.token_expiry = None

    def get_access_token(self):
        """Fetch the access token using client credentials."""
        if self.token and self.token_expiry > time.time():
            return self.token

        print("Retrieving new token...")
        payload = {
            'grant_type': 'client_credentials',
            'client_id': self.client_id,
            'client_secret': self.client_secret,
            'audience': self.audience
        }
        response = requests.post(self.token_url, data=payload)
        response_data = response.json()
        self.token = response_data['access_token']
        self.token_expiry = time.time() + response_data['expires_in'] - 60  # 60 seconds leeway

        return self.token

    def update_metadata(self, client_call_details, token):
        metadata = [('authorization', f'Bearer {token}')]
        if client_call_details.metadata is not None:
            metadata.extend(client_call_details.metadata)
        # Return a new ClientCallDetails instance with updated metadata
        return ClientCallDetails(
            client_call_details.method,
            client_call_details.timeout,
            metadata,
            client_call_details.credentials,
            client_call_details.wait_for_ready,
            client_call_details.compression
        )

    def intercept_call(self, continuation, client_call_details, request_or_iterator):
        token = self.get_access_token()
        new_call_details = self.update_metadata(client_call_details, token)
        return continuation(new_call_details, request_or_iterator)

    # Implement the intercept method for each call type using intercept_call
    def intercept_unary_unary(self, continuation, client_call_details, request):
        return self.intercept_call(continuation, client_call_details, request)

    def intercept_unary_stream(self, continuation, client_call_details, request):
        return self.intercept_call(continuation, client_call_details, request)

    def intercept_stream_unary(self, continuation, client_call_details, request_iterator):
        return self.intercept_call(continuation, client_call_details, request_iterator)

    def intercept_stream_stream(self, continuation, client_call_details, request_iterator):
        return self.intercept_call(continuation, client_call_details, request_iterator)
