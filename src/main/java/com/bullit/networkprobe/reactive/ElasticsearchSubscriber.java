package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.MDC;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.bullit.networkprobe.configuration.ElasticsearchConfiguration.INDEX;
import static com.bullit.networkprobe.support.MDCLogger.*;

@Slf4j
public class ElasticsearchSubscriber extends BaseSubscriber<ConnectionResponse> {
    private final RestHighLevelClient client;
    private final static String SUBSCRIBER_NAME = "ElasticsearchSubscriber";
    private final Supplier<SimpleDateFormat> dfSupplier;
    private final Executor executor;

    public ElasticsearchSubscriber(MDCLogger mdcLogger, RestHighLevelClient client, Supplier<SimpleDateFormat> dateFormatSupplier, Executor executor) {
        super(mdcLogger, SUBSCRIBER_NAME);
        this.client = client;
        this.dfSupplier = dateFormatSupplier;
        this.executor = executor;
    }

    @Override
    public void performOnNext(ConnectionResponse item) {
        executor.execute(() -> indexConnectionResponse(item));
    }

    private void indexConnectionResponse(ConnectionResponse item) {
        IndexRequest indexRequest = new IndexRequest(INDEX);
        indexRequest.source(
                Map.of(
                        "timestamp", dfSupplier.get().format(new Date()),
                        "responseTime", item.getResponseTime(),
                        "reachable", item.isReachable()
                )
        );

        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            mdcLogger.logWithMDCClearing(() -> {
                MDC.put(MDC_KEY, MDC_VALUE_MISSED);
                MDC.put("reachable", String.valueOf(item.isReachable()));
                MDC.put("responseTime", String.valueOf(item.getResponseTime()));
                MDC.put("server", item.getServer());
                log.info("missed");
            });
        }
    }
}
