package lv.slugboot.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import lombok.RequiredArgsConstructor;
import lv.slugboot.app.config.guac.GuacamoleTunnelHandler;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer, WebSocketMessageBrokerConfigurer {

	private final GuacamoleTunnelHandler guacamoleTunnelHandler;

	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(512 * 1024); // 512 KB message size limit
		container.setMaxBinaryMessageBufferSize(512 * 1024); // 512 KB
		return container;
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		ThreadPoolTaskScheduler heartbeatScheduler = new ThreadPoolTaskScheduler();
		heartbeatScheduler.setPoolSize(1);
		heartbeatScheduler.setThreadNamePrefix("ws-heartbeat-thread-");
		heartbeatScheduler.initialize();

		config.enableSimpleBroker("/topic").setHeartbeatValue(new long[] { 10000, 10000 })
				.setTaskScheduler(heartbeatScheduler);

		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-status").setAllowedOrigins("https://project.server-schmingus.com").withSockJS()
				.setHeartbeatTime(10_000).setDisconnectDelay(120_000);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(guacamoleTunnelHandler, "/guacamole-tunnel")
				.addInterceptors(new HttpSessionHandshakeInterceptor()).setAllowedOrigins("*");
	}

}
