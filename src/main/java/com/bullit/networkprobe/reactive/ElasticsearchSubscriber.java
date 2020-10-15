package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.MDC;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.bullit.networkprobe.support.MDCLogger.MDC_KEY;
import static com.bullit.networkprobe.support.MDCLogger.MDC_VALUE_MISSED;

@Slf4j
public class ElasticsearchSubscriber extends BaseSubscriber<ConnectionResponse> {
    private final ElasticsearchClientWrapper client;
    private final static String SUBSCRIBER_NAME = "ElasticsearchSubscriber";
    private final Supplier<SimpleDateFormat> dfSupplier;
    private final Supplier<Date> dateSupplier;
    private final BiFunction<ConnectionResponse, String, IndexRequest> createIndexRequestFun;
    private final Executor executor;

    /**
     * We are externalizing the Elasticsearch client and the function to create index requests because of
     * design issues in the Elasticsearch code that doesn't allow for proper mocking of its classes.
     * Therefore we have to wrap everything in our own classes that can be properly mocked.
     * @param mdcLogger
     * @param client
     * @param dateSupplier
     * @param dateFormatSupplier
     * @param createIndexRequestFun
     * @param executor
     */
    public ElasticsearchSubscriber(MDCLogger mdcLogger,
                                   ElasticsearchClientWrapper client,
                                   Supplier<Date> dateSupplier,
                                   Supplier<SimpleDateFormat> dateFormatSupplier,
                                   BiFunction<ConnectionResponse, String, IndexRequest> createIndexRequestFun,
                                   Executor executor) {
        super(mdcLogger, SUBSCRIBER_NAME);
        this.client = client;
        this.dfSupplier = dateFormatSupplier;
        this.dateSupplier = dateSupplier;
        this.createIndexRequestFun = createIndexRequestFun;
        this.executor = executor;
    }

    @Override
    public void performOnNext(ConnectionResponse item) {
        executor.execute(() -> indexConnectionResponse(item));
    }

    private void indexConnectionResponse(ConnectionResponse item) {
        try {
            client.index(createIndexRequestFun
                    .apply(
                            item,
                            dfSupplier.get().format(dateSupplier.get())
                    ),
                    RequestOptions.DEFAULT
            );
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
