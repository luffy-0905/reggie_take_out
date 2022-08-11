package com.java.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
