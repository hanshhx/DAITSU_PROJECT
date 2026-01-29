package com.example.TEAM202507_01.common.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// List<String> <-> DB Varchar 변환기
public class ListStringTypeHandler extends BaseTypeHandler<List<String>> {
    //해석:ListStringTypeHandler라는 클래스를 만드는데, BaseTypeHandler<List<String>>을 상속받음.

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        //해석: 자바의 데이터를 DB에 넣기 전에 변환하는 함수임. NonNull이라는 이름처럼 데이터가 null이 아닐 때만 실행됨.
        // 매겨변수:
        // ps: DB에 보낼 SQL 문장을 담고 있는 객체
        //i: 물음표(?) 중 몇 번째 위치에 넣을지 순서.
        //parameter: 실제 저장할 자바 데이터 (List<String>)

        String joined = String.join(",", parameter);
        ps.setString(i, joined);
        //해셕: String.join(",", parameter): 리스트 안에 있는 단어들을 콤마(,)로 이어 붙여서 하나의 긴 문자열로 만듬
        //해석: ps.setString(i, joined): 변환된 문자열을 DB 전송 꾸러미에 넣음.
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertToList(rs.getString(columnName));
        //해석: rs(결과표)에서 columnName(예: "menu_list")에 해당하는 값을 문자열로 꺼낸 뒤,
        // convertToList 함수로 보내서 리스트로 바꿈.
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertToList(rs.getString(columnIndex));
        //해석: 위와 똑같은데, 컬럼 이름 대신 "3번째 컬럼"처럼 번호로 찾을 때 쓰임.
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertToList(cs.getString(columnIndex));
    }
    //해석: 저장 프로시저(Stored Procedure)라는 특수한 DB 기능을 썼을 때 데이터를 받아오는 방식임.

    // String -> List (콤마로 분리)
    private List<String> convertToList(String dbData) {
        ////해석: 실제로 문자열을 리스트로 바꾸는 핵심 내부 함수임 (private이라 밖에서는 안 보임).

        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }
        //해석: 만약 DB에서 꺼낸 값이 null이거나 텅 비어있으면, 에러를 내지 말고 빈 리스트([])를 줌.
        //why?:: 데이터가 없을 때 null을 반환하면 나중에 이걸 쓰는 쪽에서 NullPointerException 에러가 날 수 있음

        return Arrays.stream(dbData.split(","))
                .map(String::trim) // 공백 제거
                .collect(Collectors.toList());
        //dbData.split(","): 콤마를 기준으로 문자열을 조각조각 자름 (배열이 됨).
        //.map(String::trim): 혹시 "짜장 , 짬뽕" 처럼 띄어쓰기가 섞여 있을까 봐 양옆 공백을 싹 제거
        //.collect(Collectors.toList()): 잘라낸 조각들을 모아서 다시 List로 만듬
    }
}