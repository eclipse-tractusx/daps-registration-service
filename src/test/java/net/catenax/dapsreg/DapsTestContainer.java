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

package net.catenax.dapsreg;

import lombok.RequiredArgsConstructor;
import net.catenax.dapsreg.service.DapsClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class DapsTestContainer implements DisposableBean {

    private final DapsClient dapsClient;

    @Value("${app.daps.imageName}")
    private String imageName;
    private GenericContainer<?> myDapsServer;

    @PostConstruct
    private void init() {
        myDapsServer = new GenericContainer<>(imageName)
                .withClasspathResourceMapping("omejdn-config", "/opt/config", BindMode.READ_WRITE)
                .withClasspathResourceMapping("omejdn-keys", "/opt/keys", BindMode.READ_WRITE)
                .withEnv("OMEJDN_PLUGINS", "/opt/config/plugins.yml")
                .withExposedPorts(4567);
        myDapsServer.start();
        dapsClient.setDapsTokenUri("http://localhost:" + myDapsServer.getMappedPort(4567) + "/token");
        dapsClient.setDapsApiUri("http://localhost:" + myDapsServer.getMappedPort(4567) + "/api/v1");
    }

    @Override
    public void destroy() {
        myDapsServer.stop();
    }
}
