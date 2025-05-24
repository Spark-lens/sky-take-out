package com.sky.mapper;

import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据 菜品id数组 ，获取套餐菜品关系
     * @param dishIds
     * @return
     */
    List<SetmealDish> getByDishIds(List<Long> dishIds);
}
