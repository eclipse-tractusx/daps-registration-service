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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.dapsreg.util.Certutil;
import org.eclipse.tractusx.dapsreg.util.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DapsManager {

    private final DapsClient dapsClient;
    private final ObjectMapper mapper;
    private final JsonUtil jsonUtil;

    public void createClient(String clientName, String securityProfile,
                             String referringConnector, MultipartFile file) throws IOException, CertificateException {

        var cert = Certutil.loadCertificate(new String(file.getBytes()));
        var clientId = Certutil.getClientId(cert);
        var clientJson = jsonUtil.getClientJson(clientId, clientName, securityProfile,referringConnector);
        Optional.of(dapsClient.createClient(clientJson)).filter(Predicate.not(HttpStatus::is2xxSuccessful)).ifPresent(httpStatus -> { throw new ResponseStatusException(httpStatus);});
        dapsClient.uploadCert(cert, clientId);
    }

    public void addAttribute(String client_id, Map<String, String> newAttr) {
        var clientAttr = dapsClient.getClient(client_id).get("attributes");
        var keys = new HashSet<>();
        var attr = Stream.concat(
                        newAttr.entrySet().stream(),
                        StreamSupport.stream(clientAttr.spliterator(), false)
                                .map(json -> Map.entry(json.get("key").asText(), json.get("value").asText()))
                ).filter(e -> keys.add(e.getKey()))
                .map(e -> mapper.createObjectNode().put("key", e.getKey()).put("value", e.getValue()))
                .collect(Collectors.toList());
        var attributes = mapper.createObjectNode();
        attributes.putArray("attributes").addAll(attr);
        dapsClient.updateClient(attributes, client_id);
    }

    public void deleteClient(String client_id) {
        dapsClient.deleteCert(client_id);
        dapsClient.deleteClient(client_id);
    }

}
