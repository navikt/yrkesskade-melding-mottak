package no.nav.yrkesskade.ysmeldingmottak;

import org.testcontainers.containers.PostgreSQLContainer;

public class IntegrationTestPostgresqlContainer extends PostgreSQLContainer<IntegrationTestPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";
    private static IntegrationTestPostgresqlContainer container;

    private IntegrationTestPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    public static IntegrationTestPostgresqlContainer getInstance() {
        if (container == null) {
            container = new IntegrationTestPostgresqlContainer();
        }

        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.datasource.url", container.getJdbcUrl());
        System.setProperty("spring.datasource.username", container.getUsername());
        System.setProperty("spring.datasource.password", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}