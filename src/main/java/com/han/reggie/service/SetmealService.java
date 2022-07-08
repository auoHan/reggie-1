package com.han.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.han.reggie.dto.SetmealDto;
import com.han.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    SetmealDto getByIdWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    void deleteWithDish(List<Long> ids);
}
