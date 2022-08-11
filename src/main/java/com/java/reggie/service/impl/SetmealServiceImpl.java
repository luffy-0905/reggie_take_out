package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.entity.Setmeal;
import com.java.reggie.mapper.SetmealMpper;
import com.java.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMpper, Setmeal> implements SetmealService {
}
