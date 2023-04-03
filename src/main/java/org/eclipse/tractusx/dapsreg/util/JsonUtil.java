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

package org.eclipse.tractusx.dapsreg.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Attr;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class JsonUtil {
    private final ObjectMapper mapper;
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private final AttributeValidator attributeValidator;

    public JsonNode getCertificateJson(X509Certificate x509Certificate) throws IOException {
        return mapper.createObjectNode().put("certificate", Certutil.getCertificate(x509Certificate));
    }

    public JsonNode getClientJson(String clientId, String clientName,
                                    String securityProfile, String referringConnector) {
        attributeValidator.validate(clientId);
        attributeValidator.validate(clientName);
        attributeValidator.validate(securityProfile);
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("client_id",
                        Optional.ofNullable(clientId)
                                .filter(Predicate.not(String::isBlank))
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId is not set")))
                .put("name",
                        Optional.ofNullable(clientName)
                                .filter(Predicate.not(String::isBlank))
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientName is not set")))
                .put("token_endpoint_auth_method", "private_key_jwt")
                .putArray("scope").add("idsc:IDS_CONNECTOR_ATTRIBUTES_ALL");
        objectNode.putArray("grant_types").add("client_credentials");
        var attr =  objectNode.putArray("attributes");
        attr.addObject()
                .put(KEY, "idsc")
                .put(VALUE, "IDS_CONNECTOR_ATTRIBUTES_ALL");
        attr.addObject()
                .put(KEY, "@type")
                .put(VALUE, "ids:DatPayload");
        attr.addObject()
                .put(KEY, "@context")
                .put(VALUE, "https://w3id.org/idsa/contexts/context.jsonld");
        attr.addObject()
                .put(KEY, "securityProfile")
                .put(VALUE, Optional.ofNullable(securityProfile).orElse("idsc:BASE_SECURITY_PROFILE"));
        if (!Objects.isNull(referringConnector)) {
            attr.addObject()
                    .put(KEY, "referringConnector")
                    .put(VALUE, referringConnector);
        }

        return objectNode;
    }
}
