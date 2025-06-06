package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Slf4j
@Api(tags = "C端用户相关接口")
@RestController
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户微信登录
     * @param userLoginDTO 微信授权码
     * @return
     */
    @ApiOperation("用户微信登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信用户登录：{}",userLoginDTO.getCode());

        // 微信登录
        User user = userService.wxlogin(userLoginDTO);

        // 为微信用户生成 jwt 令牌
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        log.info("用户 token:{}",token);

        // 构建返回数据
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())           // 用户id
                .openid(user.getOpenid())   // 微信用户唯一标识
                .token(token)               // JWT令牌
                .build();
        return Result.success(userLoginVO);
    }

}
