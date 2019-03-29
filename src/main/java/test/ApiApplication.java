package test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*@Import(value = {
    WebServerConfig.class,
    WebMvcConfig.class,
    ServicesConfig.class,
})
@ComponentScan("test.controllers")*/
@SpringBootApplication
public class ApiApplication /*extends SpringBootServletInitializer*/ {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
