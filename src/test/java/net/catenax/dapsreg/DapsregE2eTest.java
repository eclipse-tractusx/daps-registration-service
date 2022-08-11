package net.catenax.dapsreg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import net.catenax.dapsreg.util.Certutil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Objects;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DapsregE2eTest {
    @Autowired
    private DapsTestContainer dapsTestContainer;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    private JsonNode getClient(String client_id) throws Exception {
        var contentAsString = mockMvc.perform(get("/api/v1/daps/".concat(client_id))).andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var response = mapper.readValue(contentAsString, JsonNode.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return response;
    }

    @Test
    @WithMockUser(username = "fulladmin", roles={"create_daps_client", "update_daps_client", "delete_daps_client"})
    public void createRetrieveChangeDeleteTest() throws Exception {
        String clientId = null;
        try (var pemStream = Resources.getResource("cx-preprod-edc-aws.crt").openStream()) {
            var pem = new String(pemStream.readAllBytes());
            var cert = Certutil.loadCertificate(pem);
            clientId = Certutil.getClientId(cert);
            MockMultipartFile pemFile = new MockMultipartFile("file", "cx-preprod-edc-aws.crt", "text/plain", pem.getBytes());
            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/daps")
                    .file(pemFile)
                    .param("clientName", "bmw preprod")
                    .param("referringConnector", "http://connector.cx-preprod.edc.aws.bmw.cloud/BPN1234567890")
            ).andExpect(status().isCreated());
            var orig = getClient(clientId);
            assertThat(orig.get("name").asText()).isEqualTo("bmw preprod");
            mockMvc.perform(put("/api/v1/daps/".concat(clientId))
                    .param("referringConnector", "http://connector.cx-preprod.edc.aws.bmw.cloud/BPN0987654321")
                    .param("email", "admin@test.com")
            ).andExpect(status().isOk());
            var changed = getClient(clientId);
            var referringConnector = StreamSupport.stream(changed.get("attributes").spliterator(), false)
                    .filter(jsonNode -> jsonNode.get("key").asText().equals("referringConnector")).findAny().orElseThrow();
            assertThat(referringConnector.get("value").asText()).isEqualTo("http://connector.cx-preprod.edc.aws.bmw.cloud/BPN0987654321");
            var email = StreamSupport.stream(changed.get("attributes").spliterator(), false)
                    .filter(jsonNode -> jsonNode.get("key").asText().equals("email")).findAny().orElseThrow();
            assertThat(email.get("value").asText()).isEqualTo("admin@test.com");
        } finally {
            if (!Objects.isNull(clientId)) {
                mockMvc.perform(delete("/api/v1/daps/".concat(clientId))).andExpect(status().is2xxSuccessful());
            }
        }
    }
}
