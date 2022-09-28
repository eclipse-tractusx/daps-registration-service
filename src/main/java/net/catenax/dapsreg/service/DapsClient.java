/********************************************************************************
 * Copyright (c) 2021,2022 Catena-X
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package net.catenax.dapsreg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.catenax.dapsreg.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class DapsClient {

    private final static long refreshGap = 100L;

    @Value("${app.daps.apiUri}")
    @Setter
    private String dapsApiUri;
    @Value("${app.daps.tokenUri}")
    @Setter
    private String dapsTokenUri;
    @Value("${app.daps.clientId}")
    private String adminClientId;
    @Value("${app.daps.clientSecret}")
    private String adminClientSecret;

    private String dapsAdminToken = null;


    private final JsonUtil jsonUtil;
    private final ObjectMapper mapper;


    @SneakyThrows
    public String getDapsAdminToken() {
        if (!isNull(dapsAdminToken)) {
            var json = mapper.readValue(Base64.getDecoder().decode(dapsAdminToken.split("\\.")[1]), ObjectNode.class);
            if (json.get("exp").asLong() - Instant.now().getEpochSecond() > refreshGap){
                return dapsAdminToken;
            }
        }
        dapsAdminToken = fetchDapsAdminToken();
        return dapsAdminToken;
    }

    public String fetchDapsAdminToken() {
        System.out.println("dapsTokenUri:" + dapsTokenUri);
        System.out.println("client_id: " + adminClientId);
        System.out.println("client_secret: " + adminClientSecret);


        var token = WebClient.create(dapsTokenUri).post()
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", adminClientId)
                        .with("client_secret", adminClientSecret)
                        .with("scope", "omejdn:admin"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .blockOptional();
        return token.orElseThrow().get("access_token").asText();
    }

    public HttpStatus createClient(JsonNode json) {
        return WebClient.create(dapsApiUri).post()
                .uri(uriBuilder -> uriBuilder.pathSegment("config", "clients").build())
                .header("Authorization", "Bearer ".concat(getDapsAdminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }

    public HttpStatus updateClient(JsonNode json, String clientId) {
        return WebClient.create(dapsApiUri).put()
                .uri(uriBuilder -> uriBuilder.pathSegment("config", "clients", clientId).build())
                .header("Authorization", "Bearer ".concat(getDapsAdminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }

    public JsonNode getClient(String clientId) {
        return WebClient.create(dapsApiUri).get()
                .uri(uriBuilder -> uriBuilder.pathSegment("config", "clients", clientId).build())
                .header("Authorization", "Bearer ".concat(getDapsAdminToken()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .blockOptional().orElseThrow();
    }

    public HttpStatus deleteClient(String clientId) {
        return deleteSomething(uriBuilder -> uriBuilder.pathSegment("config", "clients", clientId));
    }

    public HttpStatus deleteCert(String clientId) {
        return deleteSomething(uriBuilder -> uriBuilder.pathSegment("config", "clients", clientId, "keys"));
    }

    private HttpStatus deleteSomething(UnaryOperator<UriBuilder> pathBuilder) {
        return WebClient.create(dapsApiUri).delete()
                .uri(pathBuilder.andThen(UriBuilder::build))
                .header("Authorization", "Bearer ".concat(getDapsAdminToken()))
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }

    public HttpStatus uploadCert(X509Certificate certificate, String clientId) throws IOException {
        var body = jsonUtil.getCertificateJson(certificate);
        return WebClient.create(dapsApiUri).post()
                .uri(uriBuilder -> uriBuilder.pathSegment("config", "clients", "{client_id}", "keys").build(clientId))
                .header("Authorization", "Bearer ".concat(getDapsAdminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }
}
