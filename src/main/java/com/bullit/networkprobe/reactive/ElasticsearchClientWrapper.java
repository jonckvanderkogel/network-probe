package com.bullit.networkprobe.reactive;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;

@FunctionalInterface
public interface ElasticsearchClientWrapper {
    Cancellable indexAsync(IndexRequest indexRequest, RequestOptions options, ActionListener<IndexResponse> listener);
}
