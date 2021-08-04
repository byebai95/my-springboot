package com.bai.app.dao;

import com.bai.app.model.entity.ActionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 15:04
 */
@Mapper
public interface ActionLogDao {

    Integer save(@Param("actionLog") ActionLog actionLog);
}
