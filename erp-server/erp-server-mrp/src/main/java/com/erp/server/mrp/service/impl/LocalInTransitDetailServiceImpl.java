package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.LocalInTransitDetailDTO;
import com.erp.model.mrp.entity.LocalInTransitDetailEntity;
import com.erp.model.mrp.vo.LocalInTransitDetailVO;
import com.erp.server.mrp.mapper.LocalInTransitDetailMapper;
import com.erp.server.mrp.service.LocalInTransitDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 本地在途明细 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class LocalInTransitDetailServiceImpl extends SuperServiceImpl<LocalInTransitDetailMapper, LocalInTransitDetailEntity> implements LocalInTransitDetailService {

    @Override
    public PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<LocalInTransitDetailDTO> params) {
        Page<LocalInTransitDetailVO> page = baseMapper.localInTransitDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        return new PagingVO<>(page);
    }
}
