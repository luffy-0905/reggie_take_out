package com.java.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.reggie.dto.SetmealDto;
import com.java.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    //保存套餐(同时保存关联的菜品信息)
    public void saveSetmealAndDish(SetmealDto setmealDto);
    //删除套餐(同时删除套餐与菜品的关联关系)
    public void deleteSetmealAndDish(Long[] ids);
}
