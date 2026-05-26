package it.giuval.cloud.dss_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.influxdb.v3.client.InfluxDBClient;

@Configuration
public class InfluxConfig {

    @Value("${influx.url}")
    private String influxUrl;

    @Value("${influx.token}")
    private String token;
    
    @Value("${influx.org}")
	private String organization;
    
    @Value("${influx.bucket}")
	private String bucket;

    @Bean
    public InfluxDBClient influxDBClient() {
    	return InfluxDBClient.getInstance(influxUrl, token.toCharArray(), bucket);
    }
}