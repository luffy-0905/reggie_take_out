package com.java.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.java.reggie.common.R;
import com.java.reggie.dto.DishDto;
import com.java.reggie.entity.Category;
import com.java.reggie.entity.Dish;
import com.java.reggie.entity.DishFlavor;
import com.java.reggie.service.CategoryService;
import com.java.reggie.service.DishFlavorService;
import com.java.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name)
                .orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        //List<DishDto> records1 = dishDtoPage.getRecords();这个方法获取的list不能使用list的add()和remove方法，会抛出异常
        List<DishDto> recordsDishDto=new ArrayList<>();
        for (Dish dish:records){
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
                recordsDishDto.add(dishDto);
            }
        }
        dishDtoPage.setRecords(recordsDishDto);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getDishAndFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateDishAndFlavor(dishDto);
        return R.success("更新菜品成功");
    }

    /**
     * 根据条件(categoryId)获取菜品
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        Long categoryId = dish.getCategoryId();
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //查询条件为1（菜品起售状态的）
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.eq(categoryId!=null,Dish::getCategoryId,categoryId)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }
}
