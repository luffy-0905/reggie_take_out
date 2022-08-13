package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.common.R;
import com.java.reggie.dto.DishDto;
import com.java.reggie.entity.Dish;
import com.java.reggie.entity.DishFlavor;
import com.java.reggie.mapper.DishMapper;
import com.java.reggie.service.DishFlavorService;
import com.java.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品
        this.save(dishDto);
        //获取菜品id
        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor dishFlavor:flavors){
            dishFlavor.setDishId(dishId);
        }
        //保存菜品口味数据到dish_flavor中
        //这里的flavors数据里面并没有保存dishId:所以前面foreach对其循环赋值(一个dishId可以对应多个flavor)
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和口味
     * @param id
     * @return
     */
    @Override
    public DishDto getDishAndFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);
        //复制
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        //将口味设置到菜品里
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateDishAndFlavor(DishDto dishDto) {
        //更新dish表
        this.updateById(dishDto);
        //先删除口味数据（更新flavor第一步）
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //再添加口味数据（更新flavor第二步）
        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor dishFlavor:flavors){
            dishFlavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }
}
