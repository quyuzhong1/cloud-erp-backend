package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.InitStockDetailEntity;
import com.erp.server.wms.mapper.InitStockDetailMapper;
import com.erp.server.wms.service.InitStockDetailService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname: InitStockDetailServiceImpl
 * @Description: 期初库存明细服务实现类
 * @CreateTime: 2023-05-11  10:31
 * @Author: zhangchunlin
 */
@Service
public class InitStockDetailServiceImpl extends SuperServiceImpl<InitStockDetailMapper, InitStockDetailEntity> implements InitStockDetailService {


    @Override
    public List<InitStockDetailEntity> findList(String mainId) {
        List<InitStockDetailEntity> members = lambdaQuery().eq(InitStockDetailEntity::getMainId,mainId).list();
        if(CollUtil.isNotEmpty(members)) {
            members.stream().sorted(Comparator.comparing(InitStockDetailEntity::getId)).collect(Collectors.toList());
        }
        return members;
    }

}