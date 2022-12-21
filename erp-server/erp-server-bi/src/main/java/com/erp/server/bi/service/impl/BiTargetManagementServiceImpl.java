package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiTargetManagementShowDTO;
import com.erp.model.dmp.entity.BiTargetManagementEntity;
import com.erp.server.bi.mapper.BiTargetManagementMapper;
import com.erp.server.bi.service.BiTargetManagementService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/21 17:35
 */
@Service
public class BiTargetManagementServiceImpl extends ServiceImpl<BiTargetManagementMapper, BiTargetManagementEntity> implements BiTargetManagementService {
    @Override
    public PagingVO<BiTargetManagementShowDTO> paging(PagingDTO<AdvanceSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        AdvanceSearchDTO params = dto.getParams();
        IPage<BiTargetManagementShowDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }
}
