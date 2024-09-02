package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.EstimatedDeliveryDTO;
import com.erp.model.mrp.entity.EstimatedDeliveryDetailEntity;
import com.erp.model.mrp.vo.EstimatedDeliveryVO;
import com.erp.server.mrp.mapper.EstimatedDeliveryDetailMapper;
import com.erp.server.mrp.service.EstimatedDeliveryDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 预计发货明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class EstimatedDeliveryDetailServiceImpl extends SuperServiceImpl<EstimatedDeliveryDetailMapper, EstimatedDeliveryDetailEntity> implements EstimatedDeliveryDetailService {

    @Override
    public PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<EstimatedDeliveryDTO> params) {
        Page<EstimatedDeliveryVO> page = baseMapper.estimatedDelivery(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        for (EstimatedDeliveryVO vo : page.getRecords()) {
                vo.setStatusName(ApproveStatusEnum.getName(vo.getStatus()));
        }
        return new PagingVO<>(page);
    }
}
