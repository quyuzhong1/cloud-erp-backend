package com.erp.server.mrp.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.CalcSalesInfoEstimateEntity;
import com.erp.server.mrp.mapper.CalcSalesInfoEstimateMapper;
import com.erp.server.mrp.service.CalcSalesInfoEstimateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * <p>
 * 试算销量预估 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoEstimateServiceImpl extends SuperServiceImpl<CalcSalesInfoEstimateMapper, CalcSalesInfoEstimateEntity> implements CalcSalesInfoEstimateService {


    @Override
    public List<CalcSalesInfoEstimateEntity> listByCalcSalesInfoIds(List<String> ids) {
        return list(Wrappers.<CalcSalesInfoEstimateEntity>lambdaQuery()
                .in(CalcSalesInfoEstimateEntity::getCalcSalesInfoDimId, ids));
    }
}
