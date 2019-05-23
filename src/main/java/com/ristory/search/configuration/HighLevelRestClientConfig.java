package com.ristory.search.configuration;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import com.panguyr.pgsentry.PGLogger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class HighLevelRestClientConfig {
	public static final PGLogger logger = PGLogger.create(HighLevelRestClientConfig.class);
	
	@Bean
	public RestHighLevelClient HighLevelRestClient() {
		return new RestHighLevelClient(
				RestClient.builder(new HttpHost("192.168.10.221", 9200, "http"))
					.setDefaultHeaders(new Header[] {
							new BasicHeader("accept","application/json"), 
							new BasicHeader("content-type","application/json")})
					.setFailureListener(new RestClient.FailureListener() {
						public void onFailure(Node node) {
							logger.error("High level Rest Client Failure on node " + node.getName());
						}
					}));
	}
}