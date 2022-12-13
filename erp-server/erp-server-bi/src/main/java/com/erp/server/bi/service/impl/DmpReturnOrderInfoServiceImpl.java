package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.server.bi.mapper.DmpReturnOrderInfoMapper;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import org.springframework.stereotype.Service;

/**
 * 退货订单服务
 */
@Service
public class DmpReturnOrderInfoServiceImpl extends ServiceImpl<DmpReturnOrderInfoMapper, DmpReturnOrderInfoEntity>
    implements DmpReturnOrderInfoService {

    @Override
    public PagingVO<DmpReturnOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpReturnOrderInfoSearchDTO params = dto.getParams();
        IPage<DmpReturnOrderInfoDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }
}




