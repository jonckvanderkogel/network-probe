package com.bullit.networkprobe.reactive;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * We are wrapping the RestHighLevelClient due to this bug: https://github.com/elastic/elasticsearch/issues/40534
 * Related Stackoverflow post: https://stackoverflow.com/questions/56547106/not-able-to-fully-mock-resthighlevelclient
 */
@RequiredArgsConstructor
public class ElasticsearchClientWrapperImpl implements ElasticsearchClientWrapper {
    private final RestHighLevelClient client;

    @Override
    public Cancellable indexAsync(IndexRequest indexRequest, RequestOptions options, ActionListener<IndexResponse> listener) {
        return client.indexAsync(indexRequest, options, listener);
    }
}
