package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiDataSourceDTO;
import com.erp.model.bi.entity.BiDataSourceEntity;
import com.erp.server.bi.mapper.BiDataSourceMapper;
import com.erp.server.bi.service.BiDataSourceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 9:29
 */
@Service
public class BiDataSourceServiceImpl extends ServiceImpl<BiDataSourceMapper, BiDataSourceEntity>
        implements BiDataSourceService {

    @Override
    public PagingVO<BiDataSourceDTO> paging(PagingDTO<AdvanceSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        AdvanceSearchDTO params = dto.getParams();
        IPage<BiDataSourceDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public Boolean batchAddBiDataSource(List<BiDataSourceDTO> list) {
        List<BiDataSourceEntity> biDataSourceList = BeanMapperUtils.copyList(BiDataSourceEntity.class, list);
        return this.saveBatch(biDataSourceList);
    }
}
