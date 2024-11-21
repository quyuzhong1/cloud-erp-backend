package com.erp.server.mrp.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.CalcSalesInfoFavoriteEntity;
import com.erp.server.mrp.mapper.CalcSalesInfoFavoriteMapper;
import com.erp.server.mrp.service.CalcSalesInfoFavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 试算关注表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoFavoriteServiceImpl extends SuperServiceImpl<CalcSalesInfoFavoriteMapper, CalcSalesInfoFavoriteEntity> implements CalcSalesInfoFavoriteService {

    @Override
    public List<String> listByUserId(String uid) {
        List<CalcSalesInfoFavoriteEntity> list = list(Wrappers.<CalcSalesInfoFavoriteEntity>lambdaQuery()
                .eq(CalcSalesInfoFavoriteEntity::getUserId, uid));
        return list.stream().map(CalcSalesInfoFavoriteEntity::getCfgRuleCalcId)
                .collect(Collectors.toList());
    }
}
