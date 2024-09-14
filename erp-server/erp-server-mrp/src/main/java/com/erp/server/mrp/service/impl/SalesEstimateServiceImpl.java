package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.mrp.entity.SalesEstimateEntity;
import com.erp.server.mrp.mapper.SalesEstimateMapper;
import com.erp.server.mrp.service.SalesEstimateService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 销量预估 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class SalesEstimateServiceImpl extends SuperServiceImpl<SalesEstimateMapper, SalesEstimateEntity> implements SalesEstimateService {

    @Override
    public List<SalesEstimateEntity> listByReplenishmentIdAndDay(String detailId, Integer days) {
        return list(Wrappers.<SalesEstimateEntity>lambdaQuery()
                .eq(SalesEstimateEntity::getReplenishmentDetailId, detailId)
                .between(SalesEstimateEntity::getDate, LocalDate.now(), LocalDate.now().plusDays(days))
        );
    }
}
