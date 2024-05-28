package lu.mms.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 */
@SpringBootApplication
public class SampleSpringApp {

    /**
     * @param args The app arguments.
     */
    public static void main(final String[] args) {
        System.out.println("Hello World!");
        SpringApplication.run(SampleSpringApp.class, args);
    }
}
