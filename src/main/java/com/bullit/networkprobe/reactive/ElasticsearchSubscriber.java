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
     * @param mdcLogger MDCLogger bean which allows to use the MDC mechanism for logging. We need
     *                  this instance because we are using reactive code here and then you need to
     *                  clear the MDC context before logging since you don't know which thread your
     *                  code is running on.
     * @param client ElasticsearchClientWrapper wraps the RestHighLevelClient. This is necessary
     *               because of design issues with the Elasticsearch code which doesn't allow for
     *               proper mocking of their classes.
     * @param dateSupplier a Supplier function that is used to get Date instances. Externalizing this
     *                     for test purposes.
     * @param dateFormatSupplier a Supplier function that returns a SimpleDateFormat instance since these
     *                           are not Thread safe
     * @param createIndexRequestFun function that takes a ConnectionResponse and a timestamp string and
     *                              produces an IndexRequest from these. We have to make this a function
     *                              for testing purposes since mocking a IndexRequest does not work properly.
     *                              Same issue as with the RestHighLevelClient.
     * @param executor the Executor that will run index calls to Elasticsearch
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
