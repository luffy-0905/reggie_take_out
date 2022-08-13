package com.java.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.reggie.dto.DishDto;
import com.java.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，插入菜品和口味数据（dish，dishFlavor）
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品信息和口味
    public DishDto getDishAndFlavor(Long id);
    public void updateDishAndFlavor(DishDto dishDto);
}
