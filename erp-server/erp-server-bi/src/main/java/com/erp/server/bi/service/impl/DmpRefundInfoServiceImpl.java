package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpRefundInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.server.bi.mapper.DmpRefundInfoMapper;
import com.erp.server.bi.service.DmpRefundInfoService;
import org.springframework.stereotype.Service;

/**
 * 退款列表服务类
 */
@Service
public class DmpRefundInfoServiceImpl extends ServiceImpl<DmpRefundInfoMapper, DmpRefundInfoEntity>
    implements DmpRefundInfoService {

    @Override
    public PagingVO<DmpRefundInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpReturnOrderInfoSearchDTO params = dto.getParams();
        IPage<DmpRefundInfoDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }
}




