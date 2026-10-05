package com.sri.apisentinel.riskengine;

import com.sri.apisentinel.commonmodel.ApiConsumer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyGraphTest {

    @Test
    void tracksConsumersPerApi() {
        DependencyGraph graph = new DependencyGraph();
        graph.register(ApiConsumer.of("order-service", "Customer API", "1.0.0"));
        graph.register(ApiConsumer.of("billing-service", "Customer API", "1.0.0"));
        graph.register(ApiConsumer.of("mobile-app", "Customer API", "1.0.0"));

        assertThat(graph.fanOut("Customer API")).isEqualTo(3);
        assertThat(graph.consumersOf("Customer API"))
                .containsExactlyInAnyOrder("order-service", "billing-service", "mobile-app");
    }
}