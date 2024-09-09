package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.mrp.entity.RealOutOfStockEntity;
import com.erp.server.mrp.mapper.RealOutOfStockMapper;
import com.erp.server.mrp.service.RealOutOfStockService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * <p>
 * 真实断货报告 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class RealOutOfStockServiceImpl extends SuperServiceImpl<RealOutOfStockMapper, RealOutOfStockEntity> implements RealOutOfStockService {


    @Override
    public LocalDate getRealStartDate(String id, LocalDate basicCalcDate) {
        RealOutOfStockEntity realOutOfStock = getOne(Wrappers.<RealOutOfStockEntity>lambdaQuery()
                .eq(RealOutOfStockEntity::getReplenishmentDetailId, id)
                .eq(RealOutOfStockEntity::getIsOutOfStock, false)
                .orderByDesc(RealOutOfStockEntity::getDate)
                .last("LIMIT 1")
        );
        if (realOutOfStock.getDate().plusDays(1).equals(basicCalcDate)) {
            return null;
        }else {
            return realOutOfStock.getDate().plusDays(1);
        }
    }
}
