package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.OverseasInTransitDetailEntity;
import com.erp.model.mrp.vo.OverseasInTransitDetailVO;
import com.erp.server.mrp.mapper.OverseasInTransitDetailMapper;
import com.erp.server.mrp.service.OverseasInTransitDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 海外在途明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class OverseasInTransitDetailServiceImpl extends SuperServiceImpl<OverseasInTransitDetailMapper, OverseasInTransitDetailEntity> implements OverseasInTransitDetailService {

    @Override
    public PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        Page<OverseasInTransitDetailVO> page = baseMapper.overseasInTransitDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams().getDetailId());
        for (OverseasInTransitDetailVO vo : page.getRecords()) {
            vo.setStatusName(ApproveStatusEnum.getName(vo.getStatus()));
        }
        return new PagingVO<>(page);
    }
}
