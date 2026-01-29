package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.TourDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface TourSearchRepository extends ElasticsearchRepository<TourDocument, Long> {

    @Query("{" +
            "\"multi_match\": {" +
            "   \"query\": \"?0\", " +
            "   \"fields\": [\"name^2\", \"address\", \"description\"], " +
            "   \"type\": \"cross_fields\", " +
            "   \"operator\": \"and\"" +
            "}" +
            "}")
    List<TourDocument> searchByKeyword(String keyword);
}