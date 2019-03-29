package test.configs;

import org.apache.coyote.AbstractProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class WebServerConfig {
    private final int portNumber = 8081;

    /**
     * The maximum number of request processing threads to be created.
     */
    //@Value("${server.tomcat.max-threads}")
    private final int tomcatMaxThreads = 400;

    /**
     * The minimum number of threads always kept running.
     */
    //@Value("${server.tomcat.min-spare-threads}")
    private final int tomcatMinSpareThreads = 20;

    /**
     * The maximum number of connections that the server will accept and process at any given time.
     */
    //@Value("${server.tomcat.max-connections}")
    private final int tomcatMaxConnections = 20000;

    /**
     * The maximum queue length for incoming connection requests when all possible request processing threads are in use.
     */
    //@Value("${server.tomcat.accept-count}")
    private final int tomcatAcceptCount = 400;

    /**
     * The number of milliseconds server will wait, after accepting a connection, for the request URI line to be presented.
     */
    //@Value("${server.tomcat.connection-timeout}")
    private final int tomcatConnectionTimeout = 5000;

    /**
     * The timeout for asynchronous requests in milliseconds.
     */
    //@Value("${server.tomcat.async-timeout}")
    private final int tomcatAsyncTimeout = 5000;

    @Bean
    public TomcatServletWebServerFactory webServerFactory() {
        TomcatConnectorCustomizer connectorCustomizer = connector -> {
            connector.setPort(portNumber);
            connector.setAsyncTimeout(tomcatAsyncTimeout);

            AbstractProtocol protocol = (AbstractProtocol) connector.getProtocolHandler();
            protocol.setMaxThreads(tomcatMaxThreads);
            protocol.setAcceptCount(tomcatAcceptCount);
            protocol.setMinSpareThreads(tomcatMinSpareThreads);
            protocol.setMaxConnections(tomcatMaxConnections);
            protocol.setConnectionTimeout(tomcatConnectionTimeout);
        };

        TomcatServletWebServerFactory tomcatWebServer = new TomcatServletWebServerFactory();
        tomcatWebServer.setTomcatConnectorCustomizers(Collections.singletonList(connectorCustomizer));
        return tomcatWebServer;
    }
}
