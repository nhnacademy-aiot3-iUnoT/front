package com.nhnacademy.front.admin.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminUserSort {
    CREATED_DESC("최신 가입순"),
    CREATED_ASC("오래된 가입순"),
    NAME_ASC("이름순"),
    EMAIL_ASC("이메일순"),
    ROLE_ASC("권한순"),
    STATUS_ASC("상태순");

    private final String ko;
}
