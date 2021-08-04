package com.bai.app.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 15:03
 */
@Data
public class ActionLog implements Serializable {
    private static final long serialVersionUID = 5978567193268051358L;

    private Long id;

    private String message;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
