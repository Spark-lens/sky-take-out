package com.sky.controller.admin;

import com.sky.constant.ShopConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "店铺相关接口")
@RestController("adminShopController")
@RequestMapping("/admin/shop")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺的营业状态
     * @param status
     * @return
     */
    @ApiOperation("设置营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态：{}",status == 1 ? "营业中" : "打烊中");
        // 修改redis 营业状态
        redisTemplate.opsForValue().set(ShopConstant.SHOP_STATUS_KEY,status);
        return Result.success();
    }

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
