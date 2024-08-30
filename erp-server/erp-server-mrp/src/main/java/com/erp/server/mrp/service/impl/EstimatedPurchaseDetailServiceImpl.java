package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.EstimatedPurchaseDTO;
import com.erp.model.mrp.entity.EstimatedPurchaseDetailEntity;
import com.erp.model.mrp.vo.EstimatedPurchaseVO;
import com.erp.server.mrp.mapper.EstimatedPurchaseDetailMapper;
import com.erp.server.mrp.service.EstimatedPurchaseDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 预计采购明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class EstimatedPurchaseDetailServiceImpl extends SuperServiceImpl<EstimatedPurchaseDetailMapper, EstimatedPurchaseDetailEntity> implements EstimatedPurchaseDetailService {

    @Override
    public PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<EstimatedPurchaseDTO> params) {
        Page<EstimatedPurchaseVO> page = baseMapper.estimatedPurchase(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        for (EstimatedPurchaseVO vo : page.getRecords()) {
            vo.setStatusName(ApproveStatusEnum.getName(vo.getStatus()));
        }
        return new PagingVO<>(page);
    }
}
