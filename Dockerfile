FROM openjdk:17 as build

ARG USERNAME=user
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Create the user
RUN groupadd --gid $USER_GID $USERNAME && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME

USER user

COPY . /drs/

WORKDIR /drs

RUN microdnf install dos2unix

RUN dos2unix mvnw
RUN chmod +x mvnw
RUN dos2unix .mvn/wrapper/maven-wrapper.properties

RUN ./mvnw clean install -Dmaven.test.skip=true

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM bellsoft/liberica-openjdk-alpine:17.0.3.1-2
ARG DEPENDENCY=/drs/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "app:app/lib/*", "net.catenax.dapsreg.DapsregApplication"]
EXPOSE 8080
