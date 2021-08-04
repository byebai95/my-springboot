package com.bai.app.model.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = -5906853451199646254L;

    private Long id;

    private Long uid;

    private String uname;

    private String headImg;

    private String signal;

    private String password;

    private Integer sex;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
