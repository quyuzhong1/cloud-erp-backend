package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.FbaInTransitDetailEntity;
import com.erp.model.mrp.vo.FbaInTransitDetailVO;
import com.erp.model.wms.enums.FbaDeliveryStatusEnum;
import com.erp.server.mrp.mapper.FbaInTransitDetailMapper;
import com.erp.server.mrp.service.FbaInTransitDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * fba在途明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class FbaInTransitDetailServiceImpl extends SuperServiceImpl<FbaInTransitDetailMapper, FbaInTransitDetailEntity> implements FbaInTransitDetailService {

    @Override
    public PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<FbaInTransitDetailVO> page = baseMapper.fbaInTransitDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        for (FbaInTransitDetailVO vo : page.getRecords()) {
            if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(vo.getSourceCode())) {
                vo.setStatusName(FbaDeliveryStatusEnum.getName(vo.getStatus()));
            } else {
                vo.setStatusName(ApproveStatusEnum.getName(vo.getStatus()));
            }
        }
        return new PagingVO<>(page);
    }
}
