package com.bai.app.dao;

import com.bai.app.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {

    /**
     * 根据用户uid 查询用户信息
     */
    User getUserInfoById(@Param("uid") Long uid);

}
