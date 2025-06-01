package ir.isiran.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
        Integer port = run.getEnvironment().getProperty("server.port", Integer.class);
        String contextPath = run.getEnvironment().getProperty("server.servlet.context-path", String.class);
        printServerInfo(port, contextPath);
    }

    private static void printServerInfo(Integer port, String contextPath) {
        System.out.printf("""
                           --------------------------------------------------
                           | Server URL: http://localhost:%d                 |
                           | Swagger URL: http://localhost:%d/swagger-ui.html |
                           --------------------------------------------------
                           %n""", port, port);
    }
}