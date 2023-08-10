package com.erp.server.wms.service.impl;

import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.server.wms.mapper.StocktakingPlanDetailMapper;
import com.erp.server.wms.service.StocktakingPlanDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>
 * 盘点计划明细表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Slf4j
@Service
public class StocktakingPlanDetailServiceImpl extends SuperServiceImpl<StocktakingPlanDetailMapper, StocktakingPlanDetailEntity> implements StocktakingPlanDetailService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveList(List<StocktakingPlanDTO.DetailDTO> detailList, String mainId) {
        //
    }
}
