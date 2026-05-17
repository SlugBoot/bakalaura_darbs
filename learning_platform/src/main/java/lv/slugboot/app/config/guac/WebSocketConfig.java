package lv.slugboot.app.config.guac;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer{
	
	private final GuacamoleTunnelHandler guacamoleTunnelHandler;
	
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
	    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
	    container.setMaxTextMessageBufferSize(512 * 1024);    // 512 KB message size limit
	    container.setMaxBinaryMessageBufferSize(512 * 1024);  // 512 KB
	    return container;
	}
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(guacamoleTunnelHandler, "/guacamole-tunnel")
			.addInterceptors(new HttpSessionHandshakeInterceptor())
			.setAllowedOrigins("*");
	}
	
}
