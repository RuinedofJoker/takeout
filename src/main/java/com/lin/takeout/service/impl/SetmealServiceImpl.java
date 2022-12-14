package com.lin.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lin.takeout.common.Result;
import com.lin.takeout.dto.SetmealDto;
import com.lin.takeout.entity.Category;
import com.lin.takeout.entity.Dish;
import com.lin.takeout.entity.Setmeal;
import com.lin.takeout.entity.SetmealDish;
import com.lin.takeout.mapper.CategoryMapper;
import com.lin.takeout.mapper.DishMapper;
import com.lin.takeout.mapper.SetmealDishMapper;
import com.lin.takeout.mapper.SetmealMapper;
import com.lin.takeout.service.CategoryService;
import com.lin.takeout.service.CommonService;
import com.lin.takeout.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    CommonService commonService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    DishMapper dishMapper;

    //根据套餐id获取套餐（同时获取了套餐内的菜品）
    @Override
    @Cacheable(value = "setmealCache",key = "'setmeal_'+#id")
    public Result<SetmealDto> getSetmealById(long id) {

        //查询setmeal
        Setmeal setmeal = setmealMapper.selectById(id);
        //查询setmeal所有的setmealDish存入ArrayList
        List<SetmealDish> list = setmealDishMapper.selectByDishId(setmeal.getId());

        //新建setmealDto拷贝并返回
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);

        return Result.success(setmealDto);
    }

    //套餐的分页查询
    @Override
    public Result<Page> getSetmealPage(int page, int pageSize,String name) {

        Page pageInfo = new Page<Setmeal>(page,pageSize);
        Page dtoPage = new Page<SetmealDto>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealMapper.selectPage(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {

            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);

            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryMapper.selectById(categoryId);

            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return Result.success(dtoPage);
    }

    //添加套餐，并添加套餐内的菜品
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true,condition = "#result.data != null")
    public Result<String> saveSetmeal(SetmealDto setmealDto) {

        Setmeal setmeal = setmealDto;

        //将setmeal从setmealDto中取出来，存入setmeal库（name不能重复）
        if (setmealMapper.selectByName(setmealDto.getName()) != null)
            return Result.error("套餐名字重复，添加失败");

        setmealMapper.insert(setmeal);

        //从setmeal库中取出setmealId(先存再取是因为id是数据库随机生成的，存的时候没有)
        setmeal = setmealMapper.selectByName(setmeal.getName());

        //将setmealDish从setmealDto中取出来(ArrayList),取的是dishId
        ArrayList<SetmealDish> list = (ArrayList<SetmealDish>) setmealDto.getSetmealDishes();

        for (int i = 0;i < list.size();i++){

            SetmealDish setmealDish = list.get(i);
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDish);

        }
        return Result.success("添加成功");
    }

    //修改套餐是否还有的状态(可以批量修改)
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true,condition = "#result.data != null")
    public Result<String> updateSetmealStatus(int status, String id) {

        String[] ids = id.split(",");

        for (int i = 0;i < ids.length;i++){

            if (setmealMapper.updateStatusById(status,Long.parseLong(ids[i])) == -1)
                return Result.error("修改失败");

        }
        return Result.success("修改成功");
    }

    //根据套餐分类获取对应套餐
    @Override
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId")
    public Result<List<Setmeal>> getCategoryList(Setmeal setmeal) {

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        List<Setmeal> list = setmealMapper.selectList(queryWrapper);

        return Result.success(list);
    }

    //删除套餐（可批量操作）
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true,condition = "#result.data != null")
    public Result<String> deleteSetmealById(String id) throws Exception{

        String[] ids = id.split(",");
        String[] imgs = new String[ids.length];

        for (int i = 0;i < ids.length;i++){

            imgs[i] = setmealMapper.selectById(id).getImage();

            //删除套餐内所有套餐菜品
            if (setmealDishMapper.deleteBySetmealId(Long.parseLong(ids[i])) == -1)
                return Result.error("删除套餐菜品失败");

            if (setmealMapper.deleteById(ids[i]) == -1)
                return Result.error("删除套餐失败");

            //删除对应套餐的图片
            commonService.deleteImg(imgs[i]);
        }

        return Result.success("删除成功");
    }

    //修改套餐
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true,condition = "#result.data != null")
    public Result<String> updateSetmealById(SetmealDto setmealDto) throws Exception{

        Setmeal setmeal = setmealDto;
        String oldImg = setmealMapper.selectById(setmeal.getId()).getImage();

        //暴力修改（先将套餐菜品里所有当前套餐的菜品删除然后再插入新的菜品）
        setmealDishMapper.deleteBySetmealId(setmealDto.getId());
        for (int i = 0;i < setmealDto.getSetmealDishes().size();i++){

            //根据套餐内菜品的id查询菜品信息
            Dish dish = dishMapper.selectById(setmealDto.getSetmealDishes().get(i).getDishId());
            setmealDto.getSetmealDishes().get(i).setName(dish.getName());
            setmealDto.getSetmealDishes().get(i).setPrice(dish.getPrice());
            setmealDishMapper.insert(setmealDto.getSetmealDishes().get(i));
        }

        //根据setmealDto中的setmealId查出之前图片的name
        String img = setmeal.getImage();

        //将setmeal数据进行修改（用setmealDto）
        setmealMapper.updateById(setmeal);

        //删除之前图片
        if (!oldImg.equals(img))
            commonService.deleteImg(img);

        return Result.success("更改成功");
    }

    @Override
    public Result<List<SetmealDish>> getMealDishDetails(long id) {
        return Result.success(getSetmealById(id).getData().getSetmealDishes());
    }
}
