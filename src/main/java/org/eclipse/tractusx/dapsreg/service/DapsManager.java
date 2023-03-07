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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.dapsreg.api.DapsApiDelegate;
import org.eclipse.tractusx.dapsreg.util.Certutil;
import org.eclipse.tractusx.dapsreg.util.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class DapsManager implements DapsApiDelegate {

    private final DapsClient dapsClient;
    private final ObjectMapper mapper;
    private final JsonUtil jsonUtil;

    @SneakyThrows
    @Override
    @PreAuthorize("hasAuthority(@securityRoles.createRole)")
    public ResponseEntity<Void> createClientPost(String clientName,
                                                 URI referringConnector,
                                                 MultipartFile file,
                                                 String securityProfile) {
        var cert = Certutil.loadCertificate(new String(file.getBytes()));
        var clientId = Certutil.getClientId(cert);
        var clientJson = jsonUtil.getClientJson(clientId, clientName, securityProfile, referringConnector.toString());
        Optional.of(dapsClient.createClient(clientJson)).filter(Predicate.not(HttpStatus::is2xxSuccessful)).ifPresent(httpStatus -> {
            throw new ResponseStatusException(httpStatus);
        });
        dapsClient.uploadCert(cert, clientId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasAuthority(@securityRoles.retrieveRole)")
    public ResponseEntity<Map<String, Object>> getClientGet(String clientId) {
        var jsonNode = dapsClient.getClient(clientId);
        Map<String, Object> result = mapper.convertValue(jsonNode, new TypeReference<>() {});
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority(@securityRoles.updateRole)")
    public ResponseEntity<Void> updateClientPut(String clientId, Map<String, String> newAttr) {
    var clientAttr = dapsClient.getClient(clientId).get("attributes");
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
        dapsClient.updateClient(attributes, clientId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority(@securityRoles.deleteRole)")
    public ResponseEntity<Void> deleteClientDelete(String clientId) {
        dapsClient.deleteCert(clientId);
        dapsClient.deleteClient(clientId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
