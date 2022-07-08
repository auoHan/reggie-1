package com.han.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.han.reggie.dto.DishDto;
import com.han.reggie.entity.Dish;
import com.han.reggie.entity.DishFlavor;
import com.han.reggie.mapper.DishMapper;
import com.han.reggie.service.DishFlavorService;
import com.han.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，操作dish和dish_flavor两张表
     *
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDto dishDto) {
//        新增菜品
        this.save(dishDto);
//        获取菜品id
        Long dishId = dishDto.getId();
//        用for循环的方式
        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(dishId);
        }
//        用stream的方式设置dishId的值
        /*List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(item -> {
            item.setDishId(dishId);
            return item
        }).collect(Collectors.toList());*/
//        批量新增口味
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    /**
     * 根据id查询菜品和口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    /**
     * 编辑菜品和口味，需要操作两张表
     *
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithFlavor(DishDto dishDto) {
        //1.更新菜品表
        this.updateById(dishDto);
        //2.清空对应口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //3.重新添加口味信息
        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }
}
