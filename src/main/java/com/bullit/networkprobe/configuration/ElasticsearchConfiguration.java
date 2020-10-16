package com.bullit.networkprobe.configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class ElasticsearchConfiguration {
    public static final String INDEX = "network-probe-index";

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient
                        .builder(new HttpHost("localhost", 9200, "http"))
                        .setRequestConfigCallback(config -> config
                                .setConnectTimeout(5_000)
                                .setConnectionRequestTimeout(5_000)
                                .setSocketTimeout(5_000)
                        )
                        .setHttpClientConfigCallback(
                                httpAsyncClientBuilder -> httpAsyncClientBuilder.setThreadFactory(elasticsearchThreadFactory())
                        )
        );
    }

    private ThreadFactory elasticsearchThreadFactory() {
        return new ThreadFactoryBuilder()
                .setNameFormat("elasticsearchThread-%d")
                .setDaemon(false)
                .build();
    }
}
