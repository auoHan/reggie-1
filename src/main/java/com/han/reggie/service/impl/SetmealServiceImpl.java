package com.han.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.han.reggie.common.CustomException;
import com.han.reggie.dto.SetmealDto;
import com.han.reggie.entity.Category;
import com.han.reggie.entity.Setmeal;
import com.han.reggie.entity.SetmealDish;
import com.han.reggie.mapper.SetmealMapper;
import com.han.reggie.service.CategoryService;
import com.han.reggie.service.SetmealDishService;
import com.han.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @param setmealDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithDish(SetmealDto setmealDto) {
        //先操作主表
        this.save(setmealDto);
        List<SetmealDish> setmealDishs = new ArrayList<>();
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDto.getId());
            setmealDishs.add(setmealDish);
        }
        //经过处理后再操作从表
        setmealDishService.saveBatch(setmealDishs);
    }

    /**
     * 根据ID获取回显数据
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);
        //注释报500错误，There is no getter for property named 'id' in 'class com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper'
        //如果根据主键id查询，用LambdaQueryWrapper条件查询不到，直接使用getById即可
        /*LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getId,setmeal.getCategoryId());*/
        Category category = categoryService.getById(setmeal.getCategoryId());
        if (category != null) {
            setmealDto.setCategoryName(category.getName());
        }
        return setmealDto;
    }


    /**
     * 修改套餐
     *
     * @param setmealDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class) //操作两张表以上需要使用事务
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes().stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除，批量
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class) //操作两张表以上需要使用事务
    public void deleteWithDish(List<Long> ids) {
        //1.根据ids查询需要删除的字段值,并判断status为1的数量是否大于0
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            //抛出一个自定义业务异常，并返回给前端
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //2.如果没有大于0，先正常删除setmeal表中的字段值
        this.removeByIds(ids);
        //3.还要删除关联表中的字段值
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishLambdaQueryWrapper);
    }
}
