package com.example.TEAM202507_01.config; // 1. 이 설정 파일이 위치한 패키지 경로입니다.

// 2. [Imports] MyBatis, 스프링 설정, DB 연결 등 필요한 도구들을 가져옵니다.
import io.swagger.v3.oas.models.media.XML;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import javax.sql.DataSource;

@Configuration // 3. "스프링아, 이건 단순한 코드가 아니라 '설정 파일'이야. 서버 켤 때 꼭 읽어봐." (Bean 설정 클래스 명시)
@MapperScan( // 4. MyBatis가 SQL을 실행할 자바 인터페이스(Mapper)들을 어디서 찾아야 할지 스캔 범위를 정해줍니다.
        basePackages = "com.example.TEAM202507_01", // "이 패키지 아래에 있는 모든 파일 뒤져봐."
        annotationClass = Mapper.class // "그 중에서 @Mapper 라는 이름표가 붙은 인터페이스만 진짜 매퍼로 인정해."
)
public class MyBatisConfig {

    // 5. [핵심] MyBatis의 심장인 'SqlSessionFactory'를 생성하는 메서드입니다.
    // 이 객체가 있어야 DB 연결 세션을 만들고 SQL을 실행할 수 있습니다.
    @Bean // 이 메서드가 반환하는 객체(SqlSessionFactory)를 스프링 컨테이너에 등록해서 관리하게 합니다.
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        // 6. 공장을 짓기 위한 '건설 도구(FactoryBean)'를 먼저 만듭니다.
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();

        // 7. [1. DB 연결] application.properties에 적어둔 DB 접속 정보(url, id, pw 등)가 담긴 dataSource를 연결합니다.
        sessionFactory.setDataSource(dataSource);

        // 8. [★ TypeAlias 설정] XML에서 클래스 이름을 짧게 쓰기 위한 설정입니다.
        // 이걸 설정하면 XML에서 'com.example.TEAM202507_01.user.dto.UserDto' 대신 그냥 'UserDto'나 'userDto'로 쓸 수 있습니다.
        // 이 설정이 없으면 XML에서 "Cannot find class: Community" 같은 에러가 발생합니다.
        sessionFactory.setTypeAliasesPackage("com.example.TEAM202507_01");

        // 9. [2. XML 위치 설정] 실제 SQL 쿼리가 적혀있는 .xml 파일들이 어디 있는지 알려줍니다.
        // "classpath:mappers/**/*.xml": resources/mappers 폴더 아래의 모든 폴더(**)에 있는 모든 xml 파일(*.xml)을 다 읽어오라는 뜻입니다.
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mappers/**/*.xml")
        );

        // 10. [3. 카멜케이스 설정] MyBatis의 세부 설정을 위한 설정 객체를 만듭니다.
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        // 11. DB의 스네이크 표기법(user_name)을 자바의 카멜 표기법(userName)으로 자동으로 매핑해주는 기능을 켭니다. (필수!)
        configuration.setMapUnderscoreToCamelCase(true);

        // 12. 위에서 만든 설정 객체를 팩토리에 적용합니다.
        sessionFactory.setConfiguration(configuration);

        // 13. [🔥 TypeHandler 설정] 우리가 만든 커스텀 타입 핸들러(ListStringTypeHandler 등)가 있는 위치를 알려줍니다.
        // 이걸 적어줘야 XML에서 'resultMap' 등을 쓸 때 우리가 만든 핸들러를 자동으로 찾아서 적용할 수 있습니다.
        sessionFactory.setTypeHandlersPackage("com.example.TEAM202507_01.common.handler");

        // 14. 모든 설정이 완료된 공장 객체(SqlSessionFactory)를 반환합니다. 이제 MyBatis가 작동할 준비가 끝났습니다.
        return sessionFactory.getObject();
    }
}
//
//공장 설립 (sqlSessionFactory):
//
//서버가 켜지면 스프링이 이 파일을 읽고 **"MyBatis 공장(SqlSessionFactory)"**을 짓습니다.
//
//이 공장이 있어야 나중에 adminMapper.selectStats() 같은 요청이 들어왔을 때 실제로 SQL을 돌릴 수 있습니다.
//
//재료 공급 (DataSource):
//
//공장에 가장 중요한 **데이터베이스 연결선(DataSource)**을 꽂아줍니다. (수도관 연결하듯이)
//
//약어 등록 (TypeAliases):
//
//XML 파일에서 com.example.TEAM202507_01.user.dto.UserDto라고 매번 길게 쓰면 너무 힘드니까, 그냥 UserDto라고만 써도 알아듣도록 **별명(Alias)**을 등록합니다. (이게 빠져서 에러가 났던 겁니다!)
//
//지도 전달 (MapperLocations):
//
//        "SQL 문장들이 적혀있는 XML 파일들은 resources/mappers 폴더 안에 다 모아놨으니까 거기서 찾아봐"라고 위치를 알려줍니다.
//
//        번역 규칙 (CamelCase):
//
//DB는 created_at이라고 쓰고 자바는 createdAt이라고 쓰는데, 이걸 자동으로 변환해주는 자동 번역기를 켭니다.
//
//특수 도구 등록 (TypeHandlers):
//
//아까 만들었던 ListStringTypeHandler(리스트 ↔ 문자열 변환기)가 어디에 있는지 알려줘서, 필요할 때 갖다 쓰게 합니다.