package net.catenax.dapsreg;

import lombok.RequiredArgsConstructor;
import net.catenax.dapsreg.service.DapsClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
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
