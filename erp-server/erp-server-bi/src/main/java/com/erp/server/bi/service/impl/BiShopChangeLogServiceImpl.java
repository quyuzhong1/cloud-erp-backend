package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.dmp.dto.DmpShopChangeLogDTO;
import com.erp.model.dmp.entity.BiShopChangeLogEntity;
import com.erp.server.bi.mapper.BiShopChangeLogMapper;
import com.erp.server.bi.service.BiShopChangeLogService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/15 14:30
 */
@Service
public class BiShopChangeLogServiceImpl extends ServiceImpl<BiShopChangeLogMapper, BiShopChangeLogEntity>
        implements BiShopChangeLogService {


    @Override
    public PagingVO<DmpShopChangeLogDTO> paging(PagingDTO<AdvanceSearchDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        AdvanceSearchDTO params = dto.getParams();
        IPage<DmpShopChangeLogDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }
}
