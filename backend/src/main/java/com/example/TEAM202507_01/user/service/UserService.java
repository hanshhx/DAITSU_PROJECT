package com.example.TEAM202507_01.user.service;

import com.example.TEAM202507_01.menus.mailgun.dto.mailDto;
import com.example.TEAM202507_01.user.dto.*;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();
    UserDto findById(String loginId);
    String createToken(UserSignInDto signInDto);
    void join(CreateUserDto user); // 회원가입
    void update(UserDto user);
    void delete(String loginId);
    void getTokenForFindID(String addr, String value);
    String findUserId(FindUserIdDto findUserIdDto);
    void getResetPw(ResetPasswordDto resetPasswordDto);
    boolean resetPw(String token , String email);
    void updatePw(UpdatePwDto updatePwDto) throws RuntimeException;
    boolean checkIdAvailability(String loginId);
    boolean checkEmail(String email);
    void getTokenForCheckEmail(String email, String value);
    boolean verifyEmailToken(CheckEmailDto checkEmailDto);
    UserAuthDto findUserAuthInfo(String userId);
}