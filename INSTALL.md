## Installation Steps:-

Helm charts are provided inside https://github.com/eclipse-tractusx/daps-registration-service

1.) Using helm commands:- <br />

How to install application using helm:-
    helm install ReleaseName ChartName
    
    a.) Add helm repository in tractusx:-
           helm repo add daps-reg-service https://eclipse-tractusx.github.io/charts/dev
    b.) To search the specific repo in helm repositories 
           helm search repo tractusx-dev
    c.) To install using helm command:-   
           helm install daps-reg-service tractusx-dev/daps-reg-service


2.) Local installation:

    a.) git clone https://github.com/eclipse-tractusx/daps-registration-service.git <br />
    b.) Modify values file according to your requirement.  <br />
    c.) You need to define the secrets as well in values.yaml  <br />
        secret:  <br />
          clientId:  -> Client id for DAPS.   
          clientSecret:   -> Client Secret for DAPS  <br />
          authServerUrl:   -> Auth URL for keycloak. <br />
          realm:   -> Realm for portal keycloak    <br />
          resource:   -> Resource for portal keycloak.   <br />
          apiUri:  ->   DAPS API URL.  <br />
          tokenUri:   -> DAPS token URL.  <br /> 

    d.) These secrets should be defined in Hashicorp vault. <br />
    e.) Deploy in a kubernetes cluster  <br />
        helm install daps-reg-svc charts/daps-reg-service/ -n NameSpace  <br />

