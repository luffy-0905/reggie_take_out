package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.common.CustomException;
import com.java.reggie.dto.SetmealDto;
import com.java.reggie.entity.Setmeal;
import com.java.reggie.entity.SetmealDish;
import com.java.reggie.mapper.SetmealMpper;
import com.java.reggie.service.SetmealDishService;
import com.java.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMpper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 保存套餐(同时保存关联的菜品信息)
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveSetmealAndDish(SetmealDto setmealDto) {
        //保存套餐基本信息
        this.save(setmealDto);
        //保存套餐与菜品的关联关系
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish:setmealDishList){
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    @Transactional
    public void deleteSetmealAndDish(Long[] ids) {
        //查询套餐状态，看是否可以删除（起售（status=1）不能删除）
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids)
                .eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            //说明不能删除，抛出一个异常
            throw new CustomException("套餐正在售卖,不能删除");
        }
        //可以删除，先删除setmeal表里的数据
        List<Long> list = Arrays.asList(ids);
        this.removeByIds(list);
        //再删除setmeal_dish中的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);
    }
}
