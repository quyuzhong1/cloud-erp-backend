package com.erp.server.mrp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import com.erp.server.mrp.mapper.CalcSalesInfoDimMapper;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
/**
 * <p>
 * 销量试算表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoDimServiceImpl extends SuperServiceImpl<CalcSalesInfoDimMapper, CalcSalesInfoDimEntity> implements CalcSalesInfoDimService {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void calcSalesInfo(List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList) {

    }
}
