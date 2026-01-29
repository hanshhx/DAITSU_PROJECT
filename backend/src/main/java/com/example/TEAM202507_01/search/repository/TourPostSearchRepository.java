package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.TourPostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface TourPostSearchRepository extends ElasticsearchRepository<TourPostDocument, Long> {

    @Query("{" +
            "\"multi_match\": {" +
            "   \"query\": \"?0\", " +
            "   \"fields\": [\"title^2\", \"content\"], " +
            "   \"type\": \"cross_fields\", " +
            "   \"operator\": \"and\"" +
            "}" +
            "}")
    List<TourPostDocument> searchByKeyword(String keyword);
}