package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


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


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        // 设置分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        // 获取查询 结果
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        // 获取total
        long total = page.getTotal();
        // 获取 List<DishVO>
        List<DishVO> result = page.getResult();
        // 封装为 分页结果 返回
        PageResult pageResult = new PageResult(total, result);
        return pageResult;
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        // 判断当前菜品是否能够删除---是否存在起售中的菜品？？
        List<Dish> dishList = dishMapper.getDishByIds(ids);     // 根据 菜品id数组 ，获取菜品
        for (Dish dish : dishList) {
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new RuntimeException("菜品属于在售状态，不可删除！");
            }
        }

        // 判断当前菜品是否能够删除---是否被套餐关联了？？
        List<SetmealDish> setmealDishList = setmealDishMapper.getByDishIds(ids);     // 根据 菜品id数组 ，获取套餐菜品关系
        if (setmealDishList != null && !setmealDishList.isEmpty()){
            throw new RuntimeException("菜品被套餐关联");
        }

        // 菜品表 - 删除菜品数据
        dishMapper.deleteByIds(ids);
        // 菜品口味关系表 - 删除数据
        dishFlavorsMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getByidWithFlavor(Long id) {
        // 根据 id 获取 Dish
        Dish dish = dishMapper.getDishById(id);

        // 获取 DishId 对应的 flavors 数据
        List<DishFlavor> dishFlavorList = dishFlavorsMapper.getFlavorByDishId(dish.getId());

        // 封装到 DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavorList);

        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        // 修改菜品基本信息
        dishMapper.update(dish);

        // 删除 口味，再新增口味
        dishFlavorsMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));

        // 菜品设置dishId、重新设置口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            // 重新设置菜品口味
            dishFlavorsMapper.insertBatch(flavors);
        }

    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorsMapper.getFlavorByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 菜品起售、停售
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        dishMapper.setStatus(id,status);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
}
