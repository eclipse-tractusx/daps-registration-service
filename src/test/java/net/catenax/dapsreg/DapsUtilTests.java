package net.catenax.dapsreg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Resources;
import net.catenax.dapsreg.util.Certutil;
import net.catenax.dapsreg.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
		try (var pemStream = Resources.getResource("cx-preprod-edc-aws.crt").openStream()) {
			var pem = new String(pemStream.readAllBytes());
			var cert = Certutil.loadCertificate(pem);
			var clientId = Certutil.getClientId(cert);
			assertThat(clientId).isEqualTo("A0:BE:B6:A7:BD:E1:AD:06:51:9B:D1:30:11:BD:B0:27:DB:1F:08:44:keyid:A0:BE:B6:A7:BD:E1:AD:06:51:9B:D1:30:11:BD:B0:27:DB:1F:08:44");
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
