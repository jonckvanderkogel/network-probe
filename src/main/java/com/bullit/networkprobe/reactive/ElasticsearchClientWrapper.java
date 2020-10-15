package com.bullit.networkprobe.reactive;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;

import java.io.IOException;

@FunctionalInterface
public interface ElasticsearchClientWrapper {
    IndexResponse index(IndexRequest request, RequestOptions requestOptions) throws IOException;
}
