package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.JobDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {

    @Query("{" +
            "\"multi_match\": {" +
            "   \"query\": \"?0\", " +
            "   \"fields\": [\"title^2\", \"companyName^2\", \"description\", \"companyType\"], " +
            "   \"type\": \"cross_fields\", " +
            "   \"operator\": \"and\"" +
            "}" +
            "}")
    List<JobDocument> searchByKeyword(String keyword);
}