package com.han.reggie.dto;

import com.han.reggie.entity.Setmeal;
import com.han.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
