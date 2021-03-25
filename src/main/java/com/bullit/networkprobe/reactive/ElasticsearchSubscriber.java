package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.bullit.networkprobe.support.MDCLogger.MDC_KEY;
import static com.bullit.networkprobe.support.MDCLogger.MDC_VALUE_MISSED;

@Slf4j
public class ElasticsearchSubscriber extends BaseSubscriber<ConnectionResponse> {
    private final ElasticsearchClientWrapper client;
    private final static String SUBSCRIBER_NAME = "ElasticsearchSubscriber";
    private final Supplier<ZonedDateTime> dateSupplier;
    private final DateTimeFormatter dateTimeFormatter;
    private final BiFunction<ConnectionResponse, String, IndexRequest> createIndexRequestFun;

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
     * @param dateTimeFormatter dateTimeFormatter for registering the dates of the outages
     * @param createIndexRequestFun function that takes a ConnectionResponse and a timestamp string and
     *                              produces an IndexRequest from these. We have to make this a function
     *                              for testing purposes since mocking a IndexRequest does not work properly.
     *                              Same issue as with the RestHighLevelClient.
     */
    public ElasticsearchSubscriber(MDCLogger mdcLogger,
                                   ElasticsearchClientWrapper client,
                                   Supplier<ZonedDateTime> dateSupplier,
                                   DateTimeFormatter dateTimeFormatter,
                                   BiFunction<ConnectionResponse, String, IndexRequest> createIndexRequestFun) {
        super(mdcLogger, SUBSCRIBER_NAME);
        this.client = client;
        this.dateTimeFormatter = dateTimeFormatter;
        this.dateSupplier = dateSupplier;
        this.createIndexRequestFun = createIndexRequestFun;
    }

    @Override
    public void performOnNext(ConnectionResponse item) {
        indexConnectionResponse(item)
                .doOnError(e -> {
                    mdcLogger.logWithMDCClearing(() -> {
                        MDC.put(MDC_KEY, MDC_VALUE_MISSED);
                        MDC.put("reachable", String.valueOf(item.isReachable()));
                        MDC.put("responseTime", String.valueOf(item.getResponseTime()));
                        MDC.put("server", item.getServer());
                        log.info("missed");
                    });
                })
                .subscribe();
    }

    private Mono<IndexResponse> indexConnectionResponse(ConnectionResponse item) {
        return Mono.create(sink -> client.indexAsync
                (
                        createIndexRequestFun.apply(
                                item,
                                dateTimeFormatter.format(dateSupplier.get())
                        ),
                        RequestOptions.DEFAULT,
                        new EsActionListener(sink)
                )
        );
    }

    private static class EsActionListener implements ActionListener<IndexResponse> {
        private final MonoSink<IndexResponse> sink;

        public EsActionListener(MonoSink<IndexResponse> sink) {
            this.sink = sink;
        }

        @Override
        public void onResponse(IndexResponse indexResponse) {
            sink.success(indexResponse);
        }

        @Override
        public void onFailure(Exception e) {
            sink.error(e);
        }
    }
}
