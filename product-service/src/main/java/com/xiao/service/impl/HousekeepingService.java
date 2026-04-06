package com.xiao.service.impl;

import com.xiao.entity.User;
import com.xiao.mapper.HousekeepingMapper;
import com.xiao.service.IHousekeepingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class HousekeepingService implements IHousekeepingService {

    @Resource
    private HousekeepingMapper housekeepingMapper;

    @Override
    public List<User> getUserList() {
        log.info("开始查询用户列表");
        List<User> userList = housekeepingMapper.getUserList();
        log.info("查询到用户数量: {}", userList.size());
        return userList;
    }
}
