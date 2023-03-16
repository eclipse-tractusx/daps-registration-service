## Installation Steps:-

Helm charts are provided inside https://github.com/eclipse-tractusx/daps-registration-service

1.) Using helm:  <br />
    helm install ReleaseName ChartName
    
    a.) Add helm repository in tractusx:-
           helm repo add daps-reg-service https://eclipse-tractusx.github.io/charts/dev
    b.) To search the specific repo in helm repositories 
           helm search repo tractusx-dev
    c.) To install using helm command:-   
           helm install daps-reg-service tractusx-dev/daps-reg-service


2.) Local installation:

    a.) git clone https://github.com/eclipse-tractusx/daps-registration-service.git <br />
    b.) Modify values file according to your requirement.
    c.) You need to define the secrets as well in values.yaml
        secret:
          clientId:  -> Client id for DAPS.   
          clientSecret:   -> Client Secret for DAPS
          authServerUrl:   -> Auth URL for keycloak
          realm:   -> Realm for portal keycloak
          resource:   -> Resource for portal keycloak
          apiUri:  ->   DAPS API URL
          tokenUri:   -> DAPS token URL

    d.) These secrets should be defined in Hashicorp vault
    e.) Deploy in a kubernetes cluster
        helm install daps-reg-svc charts/daps-reg-service/ -n NameSpace
