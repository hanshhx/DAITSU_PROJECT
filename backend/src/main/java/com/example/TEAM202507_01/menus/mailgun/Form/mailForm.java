package com.example.TEAM202507_01.menus.mailgun.Form;
// [1] 패키지 선언: 이 파일의 주소입니다. (메일건 메뉴 > 폼 폴더)

import javax.swing.text.html.HTML;

public class mailForm {
    // [2] 클래스 선언: 이메일 디자인 양식(Form)들을 모아놓은 공장 클래스입니다.
    // 굳이 객체를 생성(new)하지 않고도 바로 쓸 수 있게 메서드들을 static으로 만들었습니다.

    // =========================================================================
    // 1. 인증번호 메일 디자인 생성 메서드
    // =========================================================================
    // 파라미터로 받은 'code'(예: "123456")를 HTML 중간에 끼워 넣어서 리턴합니다.
    public static String codeSend(String code) {

        // 여기서부터 리턴되는 건 아주 긴 문자열(String)이지만, 실제로는 HTML 코드입니다.
        // \" : 자바 문자열 안에서 쌍따옴표(")를 쓰기 위해 역슬래시(\)를 붙인 것입니다.
        // \n : 줄바꿈 문자입니다.
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                // [HTML 시작] 이메일 클라이언트(지메일, 아웃룩 등) 호환성을 위한 표준 선언부입니다.
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" + // 한글 깨짐 방지 (UTF-8)
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" + // 모바일에서도 잘 보이게 설정
                "    <title>인증번호 확인</title>\n" + // 메일 탭 제목
                "</head>\n" +

                // [BODY 시작] 전체 배경색(#fcfdfc)과 폰트(애플고딕, 맑은고딕)를 설정합니다.
                "<body style=\"margin: 0; padding: 0; background-color: #fcfdfc; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;\">\n" +
                "    \n" +
                // [레이아웃 1] 가장 바깥쪽 투명 테이블입니다. 화면 중앙 정렬을 위해 씁니다.
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #fcfdfc; padding: 40px 0;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\" valign=\"top\">\n" +
                "                \n" +
                // [레이아웃 2] 실제 내용이 들어가는 '흰색 카드' 박스입니다.
                // 그림자(box-shadow)와 둥근 모서리(border-radius)로 예쁘게 꾸몄습니다.
                "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 480px; background-color: #ffffff; border-radius: 32px; border: 1px solid #f1f5f9; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.03);\">\n" +
                "                    \n" +
                // [디자인 포인트] 카드 맨 위에 초록색(#22c55e) 띠를 6px 두께로 그어줍니다. (브랜드 컬러 강조)
                "                    <tr>\n" +
                "                        <td height=\"6\" style=\"background-color: #22c55e;\"></td>\n" +
                "                    </tr>\n" +
                "\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 48px 40px; text-align: center;\">\n" +
                "                            \n" +
                // [아이콘] 자물쇠 이모지(🔒)를 연한 초록색 배경 원 안에 넣습니다.
                "                            <div style=\"margin: 0 auto 24px auto; width: 48px; height: 48px; background-color: #dcfce7; border-radius: 16px; line-height: 48px;\">\n" +
                "                                <span style=\"font-size: 24px;\">\uD83D\uDD12</span>\n" + // \uD83D\uDD12는 자물쇠(🔒)의 유니코드입니다.
                "                            </div>\n" +
                "\n" +
                // [메인 제목] "인증번호 확인"이라는 큰 제목을 진하게 표시합니다.
                "                            <h1 style=\"margin: 0 0 10px 0; font-size: 28px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px;\">\n" +
                "                                인증번호 확인\n" +
                "                            </h1>\n" +
                "\n" +
                // [설명 문구] 회색 글씨로 안내사항을 적습니다.
                "                            <p style=\"margin: 0 0 32px 0; font-size: 14px; font-weight: 600; color: #94a3b8; line-height: 1.6;\">\n" +
                "                                요청하신 인증번호입니다.<br>\n" +
                "                                아래 숫자를 입력창에 입력해주세요.\n" +
                "                            </p>\n" +
                "\n" +
                // [★인증번호 박스] 점선 테두리로 감싸진 박스 안에 인증번호를 크게 보여줍니다.
                "                            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 16px;\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 24px 0;\">\n" +
                "                                        <span style=\"display: block; font-size: 11px; font-weight: 800; color: #94a3b8; letter-spacing: 2px; margin-bottom: 8px;\">\n" +
                "                                            VERIFICATION CODE\n" +
                "                                        </span>\n" +
                // [핵심 데이터 삽입] 여기가 제일 중요합니다!
                // 파라미터로 받은 'code' 변수가 문자열 더하기(+)로 여기에 쏙 들어갑니다.
                "                                        <span style=\"display: block; font-size: 36px; font-weight: 900; color: #0f172a; letter-spacing: 8px; font-family: monospace;\">\n" +
                "                                            "+code+"\n" +
                "                                        </span>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "\n" +
                // [유효시간 안내] 초록색 캡슐 모양 박스 안에 "3분간 유효" 메시지를 띄웁니다.
                "                            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-top: 32px;\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\">\n" +
                "                                        <div style=\"background-color: #f0fdf4; border-radius: 50px; padding: 10px 20px; display: inline-block;\">\n" +
                "                                            <span style=\"font-size: 12px; font-weight: 700; color: #15803d;\">\n" +
                "                                                ⏱ 이 코드는 3분간 유효합니다.\n" +
                "                                            </span>\n" +
                "                                        </div>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                // [푸터/Footer] 카드 맨 아래 회색 배경 부분입니다.
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #f1f5f9;\">\n" +
                "                            <p style=\"margin: 0; font-size: 11px; color: #cbd5e1; font-weight: 500;\">\n" +
                "                                본인이 요청하지 않았다면 이 메일을 무시하세요.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "\n" +
                "                </table>\n" +
                "                </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    // =========================================================================
    // 2. 비밀번호 재설정 메일 디자인 생성 메서드
    // =========================================================================
    // 파라미터로 받은 재설정용 URL 'link'를 버튼에 연결해서 리턴합니다.
    public static String passwordSend(String link) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                // [헤더 설정] 위와 동일한 기본 설정입니다.
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
                "    <title>비밀번호 재설정</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #fcfdfc; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;\">\n" +
                "    \n" +
                // [레이아웃] 전체 틀을 잡는 테이블입니다.
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #fcfdfc; padding: 40px 0;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\" valign=\"top\">\n" +
                "                \n" +
                // [흰색 카드 박스] 내용을 담을 둥근 모서리 상자입니다.
                "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 480px; background-color: #ffffff; border-radius: 32px; border: 1px solid #f1f5f9; overflow: hidden; box-shadow: 0 10px 40px rgba(0,0,0,0.03);\">\n" +
                "                    \n" +
                // [디자인 포인트] 상단 초록색 띠
                "                    <tr>\n" +
                "                        <td height=\"6\" style=\"background-color: #22c55e;\"></td>\n" +
                "                    </tr>\n" +
                "\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 48px 40px; text-align: center;\">\n" +
                "                            \n" +
                // [아이콘] 이번엔 열쇠 이모지(🔑)를 넣었습니다.
                "                            <div style=\"margin: 0 auto 24px auto; width: 56px; height: 56px; background-color: #dcfce7; border-radius: 20px; line-height: 56px;\">\n" +
                "                                <span style=\"font-size: 28px;\">\uD83D\uDD11</span>\n" + // 🔑 유니코드
                "                            </div>\n" +
                "\n" +
                // [제목] 비밀번호 재설정
                "                            <h1 style=\"margin: 0 0 10px 0; font-size: 26px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px;\">\n" +
                "                                비밀번호 재설정\n" +
                "                            </h1>\n" +
                "\n" +
                // [안내 문구] "아래 버튼을 눌러주세요"
                "                            <p style=\"margin: 0 0 40px 0; font-size: 14px; font-weight: 600; color: #94a3b8; line-height: 1.6;\">\n" +
                "                                비밀번호 재설정 요청을 받았습니다.<br>\n" +
                "                                아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.\n" +
                "                            </p>\n" +
                "\n" +
                // [★ 버튼] 검은색 배경의 큰 버튼을 만듭니다.
                "                            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\">\n" +
                // [핵심 데이터 삽입] a 태그의 href 속성에 파라미터로 받은 'link'를 넣습니다.
                // 사용자가 이 버튼을 클릭하면 해당 링크(비밀번호 변경 페이지)로 이동합니다.
                "                                        <a href=\""+link+"\" target=\"_blank\" style=\"background-color: #0f172a; color: #ffffff; font-size: 16px; font-weight: 700; text-decoration: none; padding: 18px 32px; border-radius: 50px; display: inline-block; box-shadow: 0 10px 20px -10px rgba(15, 23, 42, 0.3);\">\n" +
                "                                            비밀번호 재설정 &rarr;\n" +
                "                                        </a>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "\n" +
                // [유효시간 안내]
                "                            <p style=\"margin-top: 32px; font-size: 12px; font-weight: 600; color: #cbd5e1;\">\n" +
                "                                이 링크는 3분 동안 유효합니다.\n" +
                "                            </p>\n" +
                "\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                // [푸터/Footer] 버튼이 안 눌릴 때를 대비한 대체 방법 안내
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8fafc; padding: 30px 40px; text-align: left; border-top: 1px solid #f1f5f9;\">\n" +
                "                            <p style=\"margin: 0 0 10px 0; font-size: 12px; font-weight: 700; color: #64748b;\">\n" +
                "                                버튼이 작동하지 않나요?\n" +
                "                            </p>\n" +
                "                            <p style=\"margin: 0; font-size: 11px; color: #94a3b8; line-height: 1.5; word-break: break-all;\">\n" +
                "                                아래 링크를 복사하여 브라우저 주소창에 붙여넣으세요:<br>\n" +
                // [보조 링크] 버튼 클릭이 안 되는 사람을 위해 텍스트 링크도 보여줍니다.
                "                                <a href=\""+link+"\" style=\"color: #22c55e; text-decoration: underline;\">\n" +
                "                                    "+link+"\n" +
                "                                </a>\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "\n" +
                "                </table>\n" +
                "                <p style=\"margin-top: 24px; font-size: 11px; color: #cbd5e1;\">\n" +
                "                    본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.\n" +
                "                </p>\n" +
                "\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
}

//상황 1: 회원가입 시 인증번호 발송 (codeSend)
//
//난수 생성: 서비스(Service)가 123456이라는 랜덤 숫자를 만듭니다.
//
//디자인 입히기: 서비스가 mailForm.codeSend("123456")을 호출합니다.
//
//HTML 생성: 이 클래스는 초록색 자물쇠 아이콘, 큰 글씨의 숫자, "3분 유효" 경고문 등이 담긴 **HTML 코드 뭉치(String)**를 리턴합니다.
//
//발송: 이 예쁜 HTML 덩어리가 메일 전송 시스템(Mailgun)을 통해 사용자의 네이버/구글 메일함으로 배달됩니다.
//
//        상황 2: 비밀번호 재설정 링크 발송 (passwordSend)
//
//링크 생성: 서비스가 https://우리사이트.com/reset?token=abc... 같은 재설정 주소를 만듭니다.
//
//디자인 입히기: mailForm.passwordSend(링크)를 호출합니다.
//
//HTML 생성: [비밀번호 재설정]이라는 큰 버튼이 달린 이메일 화면을 만들어 리턴합니다.