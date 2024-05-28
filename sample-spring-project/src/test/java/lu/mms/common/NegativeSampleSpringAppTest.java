package lu.mms.common;


import lu.mms.common.controller.MyController;
import lu.mms.common.quality.assets.bdd.ScenarioTesting;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EndToEnd test for simple App.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class NegativeSampleSpringAppTest extends ScenarioTesting {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MyController controller;

    private int port;
    private ResponseEntity<String> answer;

    @TestFactory
    public Stream<DynamicNode> shouldValidateMyFeature() {
        return super.scenariosTestFactoryTemplate();
    }

    void givenApplicationPort8080() {
        // Given
        port = 8080;
    }

    void whenCallingTheEndpointWithNoPassword() {
        // When
        answer = this.restTemplate.getForEntity("http://localhost:" + port, String.class);
    }

    void thenStatusCodeIs400() {
        // Then
        assertThat(answer.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    void andResponseBodyContainsTimeStamp() {
        // Then
        assertThat(answer.getBody(), containsString("timestamp"));
    }

    void givenAConfiguredController() {
        // Given
        assumeTrue(controller != null, "The controller cannot be null");
    }
}
