package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;


    /**
     * 新增菜品 和 对应口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        // 复制属性
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        // 新增菜品
        dishMapper.insert(dish);

        // 获取 insert 语句生成的主键值
        // Insert语句执行后，可以获取 新增菜品的 菜品id
        Long dishId = dish.getId();

        // 菜品同步新增的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            flavors.forEach(flavor -> flavor.setDishId(dishId));    // 为每个菜品口味增加 对应的菜品id
        }

        // 批量插入菜品口味(n条数据)
        dishFlavorsMapper.insertBatch(flavors);
    }
}
