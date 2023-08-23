package com.erp.server.oms.service.impl;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopCostEntity;
import com.erp.server.oms.mapper.ShopCostMapper;
import com.erp.server.oms.service.ShopCostService;
import com.common.business.service.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 店铺费用表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-22
 */
@Service
@Slf4j
public class ShopCostServiceImpl extends SuperServiceImpl<ShopCostMapper, ShopCostEntity> implements ShopCostService {

    @Override
    public BatchResultDTO setCost(String id, ShopDTO.SetCostDTO dto) {
        return null;
    }
}
