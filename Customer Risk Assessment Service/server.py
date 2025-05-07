import grpc
from concurrent import futures
import logging
import time
import risk_assessment_pb2
import risk_assessment_pb2_grpc
from db import calculate_risk, update_financial_profile, get_all_customer_ids, check_table_exists



class RiskAssessmentServicer(risk_assessment_pb2_grpc.RiskAssessmentServicer):
    def AssessRisk(self, request, context):
        """
        Single request handler to assess risk for one customer.
        This method is kept for compatibility with single-call clients.
        """
        customer_id = request.customer_id
        logging.info(f"Received request for customer_id: {customer_id}")

        try:
            # Check if the necessary tables exist
            check_table_exists()

            # Calculate the risk level for the specific customer
            risk_level = calculate_risk(customer_id)

            # Update the financial profile of the customer in the database
            update_financial_profile(customer_id, risk_level)

            logging.info(f"Risk level for customer_id {customer_id}: {risk_level}")
            return risk_assessment_pb2.RiskResponse(customer_id=customer_id, risk_level=risk_level)
        except Exception as e:
            logging.error(f"Error processing request for customer_id {customer_id}: {e}")
            context.set_details(str(e))
            context.set_code(grpc.StatusCode.UNKNOWN)
            return risk_assessment_pb2.RiskResponse(customer_id=customer_id, risk_level="UNKNOWN")

    def StreamRiskUpdates(self, request, context):
        """
        Stream risk updates for all customers continuously.
        """
        while True:  # Keep streaming risk updates periodically
            try:
                logging.info("Streaming risk updates for all customers...")
                
                # Fetch all customer IDs from the database
                customer_ids = get_all_customer_ids()

                # Process each customer ID and calculate risks
                for customer_id in customer_ids:
                    risk_level = calculate_risk(customer_id)
                    update_financial_profile(customer_id, risk_level)

                    # Stream the response for each customer
                    yield risk_assessment_pb2.RiskResponse(customer_id=customer_id, risk_level=risk_level)

                # Pause before the next cycle of updates
                time.sleep(10)  # Adjust this interval as needed
            except Exception as e:
                logging.error(f"Error during streaming risk updates: {e}")
                context.set_details(str(e))
                context.set_code(grpc.StatusCode.UNKNOWN)
                break


def serve():
    """
    Start the gRPC server and listen on port 50051.
    """
    logging.basicConfig(level=logging.INFO)
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    # Add the RiskAssessmentServicer to the server
    risk_assessment_pb2_grpc.add_RiskAssessmentServicer_to_server(RiskAssessmentServicer(), server)

    # Bind the server to a specific port
    server.add_insecure_port('[::]:50051')
    server.start()

    logging.info("Server started, listening on port 50051")
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
