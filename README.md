# <a id="introduction"></a>Catena-X DAPS Registration Service

DAPS Registration Service is a security mediator sitting 
between DAPS service and an admin user responsible for registering 
new clients (EDCs) to the DAPS. The admin user is protected with
keycloak bearer token and shall have appropriate role to create 
records at DAPS side. Therefore, the secrets for admin interface
of the DAPS are not disclosed to the requester.

# Solution Strategy 
For user authentication, Connector Registration Service relies 
on the Catena-X identity provider (keycloak). Connector 
Registration Service has access to a secret which allows 
using the remote administration plugin of the DAPS.

![Process Flow](docs/images/process-flow.png)

1. An admin user holding a bearer token with appropriate privileges
    calls REST interface of the service providing necessary information
    for registering new client (e.g. the client certificate)
