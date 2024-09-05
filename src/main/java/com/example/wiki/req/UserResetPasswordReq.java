package com.example.wiki.req;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class UserResetPasswordReq extends PageReq {

    private Long id;

    @NotNull(message = "【密码】不能为空")
    // 注意：前端经过加密后，这里的校验其实没有意义了，应该在前端加密码的正则校验
    // @Length(min = 6, max = 20, message = "【密码】6~20位")
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,32}$", message = "【密码】至少包含 数字和英文，长度6-32")
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserResetPasswordReq{");
        sb.append("id=").append(id);
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
