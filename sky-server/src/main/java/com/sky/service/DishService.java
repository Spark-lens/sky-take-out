package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.stereotype.Service;


public interface DishService {

    /**
     * 新增菜品 和 对应口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);
}
