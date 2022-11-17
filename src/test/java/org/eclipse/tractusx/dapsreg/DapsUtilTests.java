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

package org.eclipse.tractusx.dapsreg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import org.eclipse.tractusx.dapsreg.util.Certutil;
import org.eclipse.tractusx.dapsreg.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
class DapsUtilTests {

	@Autowired
	JsonUtil jsonUtil;

	@Autowired
	ObjectMapper mapper;

	@PostConstruct
	public void init() {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Test
	void utilTest() throws IOException, CertificateException {
		try (var pemStream = Resources.getResource("test.crt").openStream()) {
			var pem = new String(pemStream.readAllBytes());
			var cert = Certutil.loadCertificate(pem);
			var clientId = Certutil.getClientId(cert);
			assertThat(clientId).isEqualTo("65:FA:DE:C2:6A:58:98:D8:EA:FC:70:27:76:A0:75:D5:A1:C4:89:F9:keyid:65:FA:DE:C2:6A:58:98:D8:EA:FC:70:27:76:A0:75:D5:A1:C4:89:F9");
			var certPem = Certutil.getCertificate(cert);
			System.out.println(certPem);
			var certJson = jsonUtil.getCertificateJson(cert);
			System.out.println(toString(certJson));
			var clientRegJson = jsonUtil.getClientJson(clientId, "bmw preprod", "idsc:BASE_SECURITY_PROFILE", "http://connector.cx-preprod.edc.aws.bmw.cloud/BPN1234567890");
			System.out.println(toString(clientRegJson));
		}
	}

	private String toString(JsonNode json) throws IOException {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}

}
