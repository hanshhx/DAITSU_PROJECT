package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.HospitalDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface HospitalSearchRepository extends ElasticsearchRepository<HospitalDocument, Long> {

    @Query("{" +
            "\"multi_match\": {" +
            "   \"query\": \"?0\", " +
            "   \"fields\": [\"name^2\", \"treatCategory^2\", \"address\"], " +
            "   \"type\": \"cross_fields\", " +
            "   \"operator\": \"and\"" +
            "}" +
            "}")
    List<HospitalDocument> searchByKeyword(String keyword);
}