package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {


    /**
     * 批量插入菜品口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 菜品口味关系表 - 删除数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 获取 DishId 对应的 flavors 数据
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId};")
    List<DishFlavor> getFlavorByDishId(Long dishId);
}
