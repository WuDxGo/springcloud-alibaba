package com.xiao.service.impl;

import com.xiao.entity.User;
import com.xiao.mapper.HousekeepingMapper;
import com.xiao.service.IHousekeepingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HousekeepingService implements IHousekeepingService {

    @Resource
    private HousekeepingMapper housekeepingMapper;

    @Override
    public List<User> getUserList() {
        return housekeepingMapper.getUserList();
    }
}
