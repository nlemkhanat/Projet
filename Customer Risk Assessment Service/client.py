import grpc
import risk_assessment_pb2
import risk_assessment_pb2_grpc

def run():
    """Établir une connexion en streaming avec le serveur pour recevoir les mises à jour de risque."""
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = risk_assessment_pb2_grpc.RiskAssessmentStub(channel)
        
        # Appel au streaming pour recevoir les mises à jour
        try:
            for response in stub.StreamRiskUpdates(risk_assessment_pb2.Empty()):
                print(f"Customer ID: {response.customer_id}, Risk Level: {response.risk_level}")
        except grpc.RpcError as e:
            print(f"Error in streaming: {e.details()}")

if __name__ == '__main__':
    run()


