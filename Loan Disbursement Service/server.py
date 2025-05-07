import graphene
from flask import Flask, request, jsonify
import db_utils
from db_utils import (
    get_approved_loan,
    insert_transfer_transaction,
    get_customer_by_id
)

# Définition du type Customer
class CustomerType(graphene.ObjectType):
    id = graphene.ID()
    name = graphene.String()
    email = graphene.String()

# Définition de la réponse de la mutation
class DisbursementResponse(graphene.ObjectType):
    success = graphene.Boolean()
    message = graphene.String()
    loan_id = graphene.ID()
    transferred_amount = graphene.Float()
    customer = graphene.Field(CustomerType)

# Définition de la classe Query avec un champ hello pour éviter l'erreur
class Query(graphene.ObjectType):
    hello = graphene.String()  # Ajoute un champ simple pour tester

    def resolve_hello(self, info):
        return "Hello, world!"  # Juste un test pour voir si ça fonctionne

# Définition de la mutation de disbursement
class ConfirmLoanDisbursement(graphene.Mutation):
    class Arguments:
        loan_id = graphene.ID(required=True)

    Output = DisbursementResponse

    def mutate(self, info, loan_id):
        loan = get_approved_loan(loan_id)
        if not loan:
            return DisbursementResponse(
                success=False,
                message="Loan not approved or not found.",
                loan_id=loan_id
            )

        customer_id, amount = loan
        insert_transfer_transaction(customer_id, amount)

        # Récupération des informations du client
        customer_data = get_customer_by_id(customer_id)
        customer = CustomerType(id=customer_data[0], name=customer_data[1], email=customer_data[2])

        return DisbursementResponse(
            success=True,
            message="Funds transferred successfully",
            loan_id=loan_id,
            transferred_amount=amount,
            customer=customer
        )

# Définition de la mutation
class Mutation(graphene.ObjectType):
    confirm_loan_disbursement = ConfirmLoanDisbursement.Field()

# Création du schéma avec Query et Mutation
schema = graphene.Schema(query=Query, mutation=Mutation)

# Création de l'application Flask
app = Flask(__name__)

@app.route("/graphql", methods=["POST"])
def graphql_server():
    data = request.get_json()
    result = schema.execute(
        data.get("query"),
        variables=data.get("variables"),
    )
    return jsonify(result.data or {"errors": [str(e) for e in result.errors]})

@app.route("/")
def graphiql_ui():
    return """
    <html>
        <head>
            <title>GraphQL UI</title>
        </head>
        <body>
            <h1>Use Postman or GraphQL Playground</h1>
            <p>Send POST request to: <code>/graphql</code></p>
        </body>
    </html>
    """

if __name__ == "__main__":
    app.run(port=5000, debug=True)
