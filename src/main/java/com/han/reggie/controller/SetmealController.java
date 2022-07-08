package com.han.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.han.reggie.common.R;
import com.han.reggie.dto.SetmealDto;
import com.han.reggie.entity.Category;
import com.han.reggie.entity.Setmeal;
import com.han.reggie.service.CategoryService;
import com.han.reggie.service.SetmealDishService;
import com.han.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");
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
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, queryWrapper);
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
        List<SetmealDto> setmealDtos = new ArrayList<>();
        for (Setmeal record : setmealPage.getRecords()) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record, setmealDto);
            Category category = categoryService.getById(record.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            setmealDtos.add(setmealDto);
        }
        setmealDtoPage.setRecords(setmealDtos);
        return R.success(setmealDtoPage);
    }


    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 启售停售操作，批量
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    //可以写成数组方式接收，也可以写成下面这种集合的方式接收
//    public R<String> updateStatus(@PathVariable Integer status,  @RequestParam List<Long> ids)
//    数组方式接收
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
//        用for循环写
        /*List<Setmeal> setmeals = new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmeals.add(setmeal);
        }*/
//        用stream流写
        List<Setmeal> setmeals = Arrays.stream(ids).map(item -> {
            Setmeal setmeal = setmealService.getById(item);
            setmeal.setStatus(status);
            return setmeal;
        }).collect(Collectors.toList());
        setmealService.updateBatchById(setmeals);
        return R.success("操作成功");
    }

    /**
     * 删除，批量
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }
}
