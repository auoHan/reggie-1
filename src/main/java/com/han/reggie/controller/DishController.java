package com.han.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.han.reggie.common.R;
import com.han.reggie.dto.DishDto;
import com.han.reggie.entity.Category;
import com.han.reggie.entity.Dish;
import com.han.reggie.service.CategoryService;
import com.han.reggie.service.DishFlavorService;
import com.han.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage, queryWrapper);
//       因为Dish里没有getCategoryName字段，所以查询完后需要重新new Page，再将查询到的数据拷贝到新的page里
        Page<DishDto> dishDtoPage = new Page<>();
//        拷贝数据，但是忽略records的数据，要对records数据进行处理
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        List<DishDto> dishDtos = new ArrayList<>();
        for (Dish record : dishPage.getRecords()) {
            DishDto dishDto = new DishDto();
//            新new的DishDto是空对象，需要将原有的对象拷贝到dishDto里
            BeanUtils.copyProperties(record, dishDto);
            Category category = categoryService.getById(record.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            dishDtos.add(dishDto);
        }

//        对records重新赋值
        dishDtoPage.setRecords(dishDtos);

//        使用lambda的方式获取
        /*List<DishDto> dishDtos = dishPage.getRecords().stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());*/

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品和口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 编辑菜品和口味
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("更新成功");
    }

    /**
     * 启售停售状态修改,批量
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        /*LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Dish::getId,ids);
        updateWrapper.set(Dish::getStatus,status);
        dishService.update(updateWrapper);
        */
        List<Dish> dishes = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishes.add(dish);
        }
        dishService.updateBatchById(dishes);
        return R.success("修改成功");
    }

    /**
     * 删除菜品,批量
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids) {
//        dishService.removeById(ids);
        dishService.removeByIds(Arrays.asList(ids));
        return R.success("删除成功");
    }

    /**
     * 获取菜品集合
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
//    传过来的参数用对象接收，好处是传过来可能有多个值，通用性更好
    public R<List<Dish>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.like(dish.getName() != null, Dish::getName, dish.getName());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }

}
