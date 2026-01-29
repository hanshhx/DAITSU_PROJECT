package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.JobUserPostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface JobUserPostSearchRepository extends ElasticsearchRepository<JobUserPostDocument, Long> {

    @Query("{" +
            "\"multi_match\": {" +
            "   \"query\": \"?0\", " +
            "   \"fields\": [\"title^2\", \"companyName\", \"description\"], " +
            "   \"type\": \"cross_fields\", " +
            "   \"operator\": \"and\"" +
            "}" +
            "}")
    List<JobUserPostDocument> searchByKeyword(String keyword);
}