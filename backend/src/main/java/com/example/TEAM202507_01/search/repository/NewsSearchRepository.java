//package com.example.TEAM202507_01.search.repository;
//
//import com.example.TEAM202507_01.search.document.NewsDocument;
//import org.springframework.data.elasticsearch.annotations.Query;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import java.util.List;
//
//public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, Long> {
//
//    @Query("{" +
//            "\"multi_match\": {" +
//            "   \"query\": \"?0\", " +
//            "   \"fields\": [\"title^2\", \"content\", \"source\", \"author\"], " +
//            "   \"type\": \"cross_fields\", " +
//            "   \"operator\": \"and\"" +
//            "}" +
//            "}")
//    List<NewsDocument> searchByKeyword(String keyword);
//}