package com.erp.server.plm.service.impl;

import com.erp.server.plm.mapper.WorkOptionMapper;
import com.erp.server.plm.service.WorkOptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 工作台服务类
 * @Author Luo_WG
 * @Date 2023/4/21 15:33
 **/
@Service
public class WorkOptionServiceImpl implements WorkOptionService {

    @Resource
    private WorkOptionMapper workOptionMapper;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    public Integer getTableNum(String tableName) {
        return 0;
    }
}
