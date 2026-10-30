package com.ofix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// Permite enviar correos en segundo plano sin hacer esperar al admin
@Configuration
@EnableAsync
public class AsyncConfig {
}