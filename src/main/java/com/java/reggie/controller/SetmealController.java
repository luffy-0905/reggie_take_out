package com.java.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.java.reggie.common.R;
import com.java.reggie.dto.SetmealDto;
import com.java.reggie.entity.Category;
import com.java.reggie.entity.Setmeal;
import com.java.reggie.service.CategoryService;
import com.java.reggie.service.SetmealDishService;
import com.java.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    /**
     * 保存套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    //@CacheEvict注解是将数据从缓存中删除
    @CacheEvict(value = "setmealCache",allEntries = true)//allEntries = true是清除setmealCache下的所有缓存数据
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveSetmealAndDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo=new Page<>();
        Page<SetmealDto> dtoPage=new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name)
                .orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> recordsSetmealDto=new ArrayList<>();
        for(Setmeal setmeal:records){
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            //分类id
            Long categoryId = setmeal.getCategoryId();
            //根据id查询分类信息
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
                recordsSetmealDto.add(setmealDto);
            }
        }
        dtoPage.setRecords(recordsSetmealDto);
        return R.success(dtoPage);
    }
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//allEntries = true是清除setmealCache下的所有缓存数据
    public R<String> deleteSetmeal(Long[] ids){
        log.info("ids:{}",ids);
        setmealService.deleteSetmealAndDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    //@Cacheable:先判断缓存中是否有数据，有就直接返回数据，如果没有，就将方法返回的数据放到缓存中
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status",unless = "#result==null")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId())
                .eq(setmeal.getStatus()!=null,Setmeal::getStatus,1)
                .orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }
}
