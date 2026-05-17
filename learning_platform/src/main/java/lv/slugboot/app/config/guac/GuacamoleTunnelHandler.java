package lv.slugboot.app.config.guac;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.slugboot.app.models.LabInstance;
import lv.slugboot.app.service.ILabInstanceCRUDService;

@Component
@Slf4j
@RequiredArgsConstructor
public class GuacamoleTunnelHandler extends TextWebSocketHandler {

	private final ILabInstanceCRUDService labInstanceCRUDService;
    private static final String TUNNEL_ATTRIBUTE = "GUAC_TUNNEL";
    
    private final ExecutorService guacReaderExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Ensure threads don't block application shutdown
        return t;
    });

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> queryParams = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .toSingleValueMap();
        
        String instanceIdStr = queryParams.get("instanceId");

        if (instanceIdStr != null && instanceIdStr.contains("?")) {
            instanceIdStr = instanceIdStr.split("\\?")[0];
        }

        if (instanceIdStr == null || instanceIdStr.isEmpty()) {
            log.error("Missing instanceId parameter.");
            session.close(CloseStatus.BAD_DATA.withReason("Missing instanceId"));
            return;
        }

        UUID instanceId = UUID.fromString(instanceIdStr);
        
        try {
            LabInstance instance = labInstanceCRUDService.retrieveById(instanceId);
            String containerIp = instance.getIpAddress();
            
            if (containerIp == null || containerIp.isEmpty()) {
                session.close(CloseStatus.BAD_DATA.withReason("IP not allocated"));
                return;
            }
            
            log.info("Connecting Guacamole tunnel to container IP: {}", containerIp);
            
            GuacamoleConfiguration config = new GuacamoleConfiguration();
            config.setProtocol("ssh");
            config.setParameter("hostname", containerIp);
            config.setParameter("port", "22");
            config.setParameter("username", "root");
            config.setParameter("password", "securepassword");
//            config.setParameter("command", "/bin/bash");
            config.setParameter("terminal-type", "linux");
            config.setParameter("enable-sftp", "false");
            config.setParameter("width", "1024");
            config.setParameter("height", "768");
            config.setParameter("dpi", "96");
            
            // Connect to your local guacd daemon
            GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822), config
            );
            
            GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
            session.getAttributes().put(TUNNEL_ATTRIBUTE, tunnel);
            
            String tunnelId = tunnel.getUUID().toString();
            String handshakeInstruction = "0.," + tunnelId.length() + "." + tunnelId + ";";
            
            // 5000ms execution timeout, 256KB buffer limit
            WebSocketSession concurrentSession = new ConcurrentWebSocketSessionDecorator(session, 5000, 262144);
            session.getAttributes().put("CONCURRENT_SESSION", concurrentSession);
            
            if (concurrentSession.isOpen()) {
            	concurrentSession.sendMessage(new TextMessage(handshakeInstruction));
            }
            
            
            guacReaderExecutor.submit(() -> {
                Thread.currentThread().setName("guac-reader-" + instanceId);
                try {
                    GuacamoleReader reader = tunnel.getSocket().getReader();
                    char[] buffer;
                    
                    // Loop runs continuously as long as the underlying tunnel remains open
                    while (tunnel.isOpen() && (buffer = reader.read()) != null) {
                        
                        // Dynamic look-up ensures we are always grabbing the live, decorated concurrent session instance
                        WebSocketSession activeConcurrentSession = (WebSocketSession) session.getAttributes().get("CONCURRENT_SESSION");
                        
                        if (activeConcurrentSession != null && activeConcurrentSession.isOpen()) {
                            activeConcurrentSession.sendMessage(new TextMessage(new String(buffer)));
                        } else {
                            // If the client truly dropped or closed, we stop the thread
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Tunnel read loop terminated or socket timed out: {}", e.getMessage());
                } finally {
                    closeTunnel(session);
                }
            });
            
        } catch (Exception e) {
            log.error("Tunnel building failed", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
	
	@Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Receive instructions from browser/thymeleaf layer and pipe them straight into guacd
        GuacamoleTunnel tunnel = (GuacamoleTunnel) session.getAttributes().get(TUNNEL_ATTRIBUTE);
        WebSocketSession concurrentSession = (WebSocketSession) session.getAttributes().get("CONCURRENT_SESSION");

        // Ensure we reference the active session condition checking against our thread-safe wrapper
        if (tunnel != null && tunnel.isOpen() && concurrentSession != null && concurrentSession.isOpen()) {
            try {
                GuacamoleWriter writer = tunnel.getSocket().getWriter();
                synchronized (tunnel) {
                    writer.write(message.getPayload().toCharArray());
                }
            } catch (GuacamoleException e) {
                log.error("Failed writing text frame instruction packet to guacd socket", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed by client. Tidying up Guacamole tunnel.");
        closeTunnel(session);
    }

    private void closeTunnel(WebSocketSession session) {
        GuacamoleTunnel tunnel = (GuacamoleTunnel) session.getAttributes().remove(TUNNEL_ATTRIBUTE);
        if (tunnel != null) {
            try {
                tunnel.close();
                log.info("Guacamole tunnel closed successfully.");
            } catch (GuacamoleException e) {
                log.error("Error error closing Guacamole tunnel", e);
            }
        }
    }
}

