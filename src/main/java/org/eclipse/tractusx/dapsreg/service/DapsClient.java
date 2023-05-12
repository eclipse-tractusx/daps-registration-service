/********************************************************************************
 * Copyright (c) 2021,2022 T-Systems International GmbH
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

package org.eclipse.tractusx.dapsreg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.dapsreg.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DapsClient {

    private static final long REFRESH_GAP = 100L;
    private static final String[] PATH = "config/clients".split("/");

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
            if (json.get("exp").asLong() - Instant.now().getEpochSecond() > REFRESH_GAP){
                return dapsAdminToken;
            }
        }
        dapsAdminToken = fetchDapsAdminToken();
        return dapsAdminToken;
    }

    private void headersSetter(HttpHeaders headers) {
        headers.add("Authorization", "Bearer ".concat(getDapsAdminToken()));
    }

    public String fetchDapsAdminToken() {
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

    public Optional<ResponseEntity<Void>> createClient(JsonNode json) {
        return WebClient.create(dapsApiUri).post()
                .uri(uriBuilder -> uriBuilder.pathSegment(PATH).build())
                .headers(this::headersSetter)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .retrieve()
                .toBodilessEntity()
                .blockOptional();
    }

    public HttpStatus updateClient(JsonNode json, String clientId) {
        return (HttpStatus) WebClient.create(dapsApiUri).put()
                .uri(uriBuilder -> uriBuilder.pathSegment(PATH).pathSegment(clientId).build())
                .headers(this::headersSetter)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }

    public Optional<JsonNode> getClient(String clientId) {
        return WebClient.create(dapsApiUri).get()
                .uri(uriBuilder -> uriBuilder.pathSegment(PATH).pathSegment(clientId).build())
                .headers(this::headersSetter)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onRawStatus(code -> code == 404, clientResponse -> Mono.empty())
                .bodyToMono(JsonNode.class)
                .blockOptional();
    }

    public HttpStatus deleteClient(String clientId) {
        return deleteSomething(uriBuilder -> uriBuilder.pathSegment(PATH).pathSegment(clientId));
    }

    public HttpStatus deleteCert(String clientId) {
        return deleteSomething(uriBuilder -> uriBuilder.pathSegment(PATH).pathSegment(clientId, "keys"));
    }

    private HttpStatus deleteSomething(UnaryOperator<UriBuilder> pathBuilder) {
        return (HttpStatus) WebClient.create(dapsApiUri).delete()
                .uri(pathBuilder.andThen(UriBuilder::build))
                .headers(this::headersSetter)
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }

    public HttpStatus uploadCert(X509Certificate certificate, String clientId) throws IOException {
        var body = jsonUtil.getCertificateJson(certificate);
        return (HttpStatus) WebClient.create(dapsApiUri).post()
                .uri(uriBuilder -> uriBuilder.pathSegment(PATH).pathSegment( clientId, "keys").build())
                .headers(this::headersSetter)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .blockOptional().orElseThrow().getStatusCode();
    }
}