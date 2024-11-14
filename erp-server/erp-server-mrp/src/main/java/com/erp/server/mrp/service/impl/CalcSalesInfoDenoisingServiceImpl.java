package com.erp.server.mrp.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.CalcSalesInfoDenoisingEntity;
import com.erp.server.mrp.mapper.CalcSalesInfoDenoisingMapper;
import com.erp.server.mrp.service.CalcSalesInfoDenoisingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * <p>
 * 试算销量去噪 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoDenoisingServiceImpl extends SuperServiceImpl<CalcSalesInfoDenoisingMapper, CalcSalesInfoDenoisingEntity> implements CalcSalesInfoDenoisingService {

    @Override
    public List<CalcSalesInfoDenoisingEntity> listByCalcSalesInfoId(String calcSalesInfoId) {
        return list(Wrappers.<CalcSalesInfoDenoisingEntity>lambdaQuery()
                .eq(CalcSalesInfoDenoisingEntity::getCalcSalesInfoDimId, calcSalesInfoId));
    }
}
