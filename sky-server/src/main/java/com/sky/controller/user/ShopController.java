package com.sky.controller.user;

import com.sky.constant.ShopConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "店铺相关接口")
@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取营业状态
     * @return
     */
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        // 获取营业状态
        Integer status = (Integer) redisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS_KEY);
        log.info("店铺的营业状态：{}",status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
