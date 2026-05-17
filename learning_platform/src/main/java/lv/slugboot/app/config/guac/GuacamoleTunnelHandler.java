package lv.slugboot.app.config.guac;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
	
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String query = session.getUri().getQuery();
		
		if (query == null || query.trim().isEmpty()) {
	        log.error("Guacamole Tunnel Handshake aborted: Entire query string is missing.");
	        session.close(CloseStatus.BAD_DATA.withReason("Missing query parameters."));
	        return;
	    }
		
		Map<String, String> queryParams = UriComponentsBuilder.fromUriString("?" + query)
				.build().getQueryParams().toSingleValueMap();
		
		String instanceIdStr = queryParams.get("instanceId");
		
		if (instanceIdStr == null && query.contains("instanceId=")) {
	        instanceIdStr = query.split("instanceId=")[1].split("&")[0];
	    }
		
		if (instanceIdStr == null || instanceIdStr.trim().isEmpty()) {
	        log.error("Guacamole Tunnel Handshake aborted: query parameter 'instanceId' is empty or missing.");
	        session.close(CloseStatus.BAD_DATA.withReason("Required connection parameter query 'instanceId' was missing."));
	        return;
	    }
		
		if (instanceIdStr.contains("?")) {
	        instanceIdStr = instanceIdStr.split("\\?")[0];
	    }
	    if (instanceIdStr.contains("&")) {
	        instanceIdStr = instanceIdStr.split("&")[0];
	    }

		log.info("Sanitized UUID hitting Backend String Parser: '{}' (Length: {})", instanceIdStr, instanceIdStr.length());
		
		try {
			UUID instanceId = UUID.fromString(instanceIdStr);
			LabInstance instance = labInstanceCRUDService.retrieveById(instanceId);
			
			String containerIp = instance.getIpAddress();
			if (containerIp == null || containerIp.isEmpty()) {
				session.close(CloseStatus.BAD_DATA.withReason("Container IP not allocated"));
				return;
			}
			
			log.info("Opening Guacamole SSH tunnel to container IP: {}", containerIp);
			
			GuacamoleConfiguration config = new GuacamoleConfiguration();
			config.setProtocol("ssh");
			config.setParameter("hostname", containerIp);
			config.setParameter("port", "22");
			config.setParameter("username", "root");
			config.setParameter("password", "securepassword");

			config.setParameter("command", "/bin/bash");
			config.setParameter("terminal-type", "linux");
			config.setParameter("enable-sftp", "false");
			
			GuacamoleSocket socket = new ConfiguredGuacamoleSocket(new InetGuacamoleSocket("localhost", 4822), config);
			
			GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
            session.getAttributes().put(TUNNEL_ATTRIBUTE, tunnel);
            
            GuacamoleReader reader = socket.getReader();
            
            String tunnelId = tunnel.getUUID().toString();
            String handshakeInstruction = "0.,connection," + tunnelId.length() + "." + tunnelId + ";";
            session.sendMessage(new TextMessage(handshakeInstruction));
            
            Thread readThread = new Thread(() -> {
                try {
                    char[] buffer;
                    // FIX: Ensure the stream keeps listening and handling exceptions gracefully without crashing out early
                    while (session.isOpen() && (buffer = reader.read()) != null) {
                        if (buffer.length > 0) {
                            session.sendMessage(new TextMessage(new String(buffer)));
                        }
                    }
                } catch (IOException | GuacamoleException e) {
                    log.debug("Guacamole tunnel read stream closed/interrupted: {}", e.getMessage());
                } finally {
                    closeTunnel(session);
                }
            });
            readThread.setName("guac-reader-" + instanceIdStr);
            readThread.start();
            
		} catch (Exception e) {
			log.error("Failed to construct Guacamole tunnel connection", e);
			session.close(CloseStatus.SERVER_ERROR.withReason(e.getMessage()));
		}
	}
	
	@Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Receive instructions from browser/thymeleaf layer and pipe them straight into guacd
        GuacamoleTunnel tunnel = (GuacamoleTunnel) session.getAttributes().get(TUNNEL_ATTRIBUTE);
        if (tunnel != null) {
            GuacamoleWriter writer = tunnel.getSocket().getWriter();
            writer.write(message.getPayload().toCharArray());
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

