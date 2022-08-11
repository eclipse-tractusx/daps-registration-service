package net.catenax.dapsreg;

import net.catenax.dapsreg.service.DapsClient;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DapsClientTests {
    @Autowired
    private DapsClient dapsClient;
    @Autowired
    private DapsTestContainer dapsTestContainer;

    @Test
    public void getAdminTokenTest() {
        var adminToken = dapsClient.getDapsAdminToken();
        assertThat(adminToken).isNotNull();
        assertThat(adminToken).isEqualTo(dapsClient.getDapsAdminToken());
        assertThat(adminToken).isNotEqualTo(dapsClient.fetchDapsAdminToken());
    }

}
