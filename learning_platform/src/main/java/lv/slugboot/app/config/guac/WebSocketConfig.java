package lv.slugboot.app.config.guac;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer{
	
	private final GuacamoleTunnelHandler guacamoleTunnelHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(guacamoleTunnelHandler, "/guacamole-tunnel")
			.addInterceptors(new HttpSessionHandshakeInterceptor())
			.setAllowedOrigins("*");
	}
	
}
