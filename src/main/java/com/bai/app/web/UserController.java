package com.bai.app.web;

import com.bai.app.annotation.OperatorAction;
import com.bai.app.model.entity.User;
import com.bai.app.service.UserService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "UserController",tags = "User")
@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @OperatorAction(value = "查询用户信息")
    @GetMapping(value = "getUserInfoById")
    public User getUserInfoById(@RequestParam("uid")Long uid){
        return userService.getUserInfoById(uid);
    }
}
