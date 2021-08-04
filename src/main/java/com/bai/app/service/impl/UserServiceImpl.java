package com.bai.app.service.impl;

import com.bai.app.dao.UserDao;
import com.bai.app.model.entity.User;
import com.bai.app.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 11:39
 */
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public User getUserInfoById(Long uid) {
        return userDao.getUserInfoById(uid);
    }
}
