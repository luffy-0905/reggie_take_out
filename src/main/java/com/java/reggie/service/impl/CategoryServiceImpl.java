package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.common.CustomException;
import com.java.reggie.entity.Category;
import com.java.reggie.entity.Dish;
import com.java.reggie.entity.Setmeal;
import com.java.reggie.mapper.CategoryMapper;
import com.java.reggie.service.CategoryService;
import com.java.reggie.service.DishService;
import com.java.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类，删除之前需要判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        //查看分类是否关联了菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if(count1>0){
            //说明已经关联菜品，抛出异常
            throw new CustomException("当前菜品下关联了菜品,不能删除");
        }
        //查看分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0){
            //说明分类关联了套餐，抛出异常
            throw new CustomException("当前菜品下关联了套餐,不能删除");
        }
        super.removeById(id);
    }
}
