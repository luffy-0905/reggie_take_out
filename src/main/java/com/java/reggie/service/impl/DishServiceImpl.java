package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.common.CustomException;
import com.java.reggie.common.R;
import com.java.reggie.dto.DishDto;
import com.java.reggie.entity.Dish;
import com.java.reggie.entity.DishFlavor;
import com.java.reggie.entity.Setmeal;
import com.java.reggie.entity.SetmealDish;
import com.java.reggie.mapper.DishMapper;
import com.java.reggie.service.DishFlavorService;
import com.java.reggie.service.DishService;
import com.java.reggie.service.SetmealDishService;
import com.java.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
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

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDish(Long[] ids) {
        //先判断菜品的售卖状态能否删除
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids)
                .eq(Dish::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            //说明有菜品正在售卖不能删除，抛出异常
            throw new CustomException("菜品正在售卖,不能删除");
        }
        //再判断菜品是否与套餐关联
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(queryWrapper1);
        if(count1>0){
            //说明菜品与套餐有关联
            throw new CustomException("菜品与套餐相关联,不能删除");
        }
        //到这里，说明能删除dish(removeByIds()这个方法不能传入数组，这里将数组转换成list)
        List<Long> list = Arrays.asList(ids);
        this.removeByIds(list);
    }

    /**
     * 起售或停售菜品
     * @param status
     * @param ids
     */
    @Override
    @Transactional
    public void updateStatus(Integer status, Long[] ids) {
        //停售
        if(status==0){
            //先考虑即将停售菜品与套餐的是否关联，并判断套餐是否停售
            LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.in(ids!=null,SetmealDish::getDishId,ids);
            List<SetmealDish> list = setmealDishService.list(queryWrapper);
            //判断是否为空
            if(list!=null && list.size()>0){
                Set<Long> set=new HashSet<>();
                //循环遍历出setmealId,保存到set集合（保证setmealId不重复）
                for(SetmealDish setmealDish:list){
                    set.add(setmealDish.getSetmealId());
                }

                LambdaQueryWrapper<Setmeal> queryWrapper1=new LambdaQueryWrapper<>();
                queryWrapper1.in(set!=null,Setmeal::getId,set);
                List<Setmeal> setmealList = setmealService.list(queryWrapper1);
                for (Setmeal setmeal:setmealList){
                    if(setmeal.getStatus()==1){
                        throw new CustomException("此菜品与套餐关联,并且该套餐正在出售，不能停售该菜品");
                    }
                }
            }
            //到这里，说明可以停售该菜品
            LambdaQueryWrapper<Dish> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.in(ids!=null,Dish::getId,ids);
            List<Dish> dishList = this.list(queryWrapper2);
            for(Dish dish:dishList){
                dish.setStatus(status);
                this.updateById(dish);
            }
        }else {
            //起售
            LambdaQueryWrapper<Dish> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.in(ids!=null,Dish::getId,ids);
            List<Dish> dishList = this.list(queryWrapper3);
            for(Dish dish:dishList){
                dish.setStatus(status);
                this.updateById(dish);
            }
        }
    }
}
