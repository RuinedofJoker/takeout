package com.lin.reggie.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lin.reggie.common.Result;
import com.lin.reggie.entity.Dish;
import com.lin.reggie.mapper.OrderMapper;
import com.lin.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;

    @Override
    public Result<Page> getOrderPageList(int page, int pageSize) {        Page pageInfo = new Page<Dish>(page,pageSize);
        orderMapper.selectPage(pageInfo,null);
        return Result.success(pageInfo);
    }
}