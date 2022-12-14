package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.dmp.entity.BiDataSourceCostEntity;
import com.erp.server.bi.mapper.BiDataSourceCostMapper;
import com.erp.server.bi.service.BiDataSourceCostService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:37
 */
@Service
public class BiDataSourceCostServiceImpl extends ServiceImpl<BiDataSourceCostMapper, BiDataSourceCostEntity>
        implements BiDataSourceCostService {

    @Override
    public PagingVO<BiDataSourceCostDTO> paging(PagingDTO<BiDataSourceCostSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BiDataSourceCostSearchDTO params = dto.getParams();
        IPage<BiDataSourceCostDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }
}
