package com.bai.app.service;

import com.bai.app.model.entity.User;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 11:38
 */
public interface UserService {

    /**
     * 根据用户id 查询用户信息
     * @param uid
     * @return
     */
    User getUserInfoById(Long uid);

}
