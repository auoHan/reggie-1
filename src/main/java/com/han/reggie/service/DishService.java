package com.han.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.han.reggie.dto.DishDto;
import com.han.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，修改菜品和口味对应的两张表
    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
