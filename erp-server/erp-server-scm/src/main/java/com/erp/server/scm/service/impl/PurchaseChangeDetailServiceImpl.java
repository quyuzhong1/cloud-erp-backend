package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;
import com.erp.server.scm.mapper.PurchaseChangeDetailMapper;
import com.erp.server.scm.service.PurchaseChangeDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseChangeDetailServiceImpl extends SuperServiceImpl<PurchaseChangeDetailMapper, PurchaseChangeDetailEntity> implements PurchaseChangeDetailService {

    @Override
    public void add(List<PurchaseChangeDetailDTO.AddDTO> details, String purchaseChangeId) {

    }
}
