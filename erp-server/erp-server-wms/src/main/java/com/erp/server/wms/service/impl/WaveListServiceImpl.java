package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.server.wms.mapper.WaveListMapper;
import com.erp.server.wms.service.WaveListService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 波次拣货
 * @date 2024-06-28
 * @author tanmujin
 */
@Service
public class WaveListServiceImpl extends SuperServiceImpl<WaveListMapper, WaveListEntity> implements WaveListService {
    @Override
    public PagingVO<WaveListDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO) {
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<WaveListEntity> result = this.baseMapper.paging(page, pagingDTO.getParams());
        List<WaveListDTO.ViewDTO> viewDTOList = fillViewList(result.getRecords());
        return new PagingVO<>(viewDTOList, (int)result.getTotal(), (int)result.getSize(), (int)result.getCurrent());
    }

    private List<WaveListDTO.ViewDTO> fillViewList(List<WaveListEntity> records) {
        return null;
    }

    @Override
    public List<WaveListDTO.TabDTO> tabList() {
        return Collections.emptyList();
    }
}
