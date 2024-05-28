package lu.mms.common;


import lu.mms.common.controller.MyController;
import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EndToEnd test for simple App.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PositiveSampleSpringAppTest extends ScenarioTesting {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MyController controller;

    private int port;
    private String answer;

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void givenApplicationPort8080() {
        // Given
        port = 8080;
    }

    void whenCallingTheEndpointWithValidPassword() {
        // When
        answer = this.restTemplate.getForObject("http://localhost:" + port + "?password=12547", String.class);
    }

    void thenTheAnswerContainsTheWordApp() {
        // Then
        assertThat(answer, Matchers.containsStringIgnoringCase("App"));
    }

    void andThenContainsTheWordRestAsWell() {
        // Then
        assertThat(answer, Matchers.containsStringIgnoringCase("rest"));
    }

    void givenAConfiguredController() {
        // Given
        assumeTrue(controller != null, "The controller cannot be null");
    }
}
