package com.example.TEAM202507_01.search.repository;

import com.example.TEAM202507_01.search.document.CommunityPostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface CommunityPostSearchRepository extends ElasticsearchRepository<CommunityPostDocument, Long> {

    @Query("{" +
            "\"multi_match\": {" +
            "   \"query\": \"?0\", " +
            "   \"fields\": [\"title^2\", \"content\", \"userNickname\"], " +
            "   \"type\": \"cross_fields\", " +
            "   \"operator\": \"and\"" +
            "}" +
            "}")
    List<CommunityPostDocument> searchByKeyword(String keyword);
}