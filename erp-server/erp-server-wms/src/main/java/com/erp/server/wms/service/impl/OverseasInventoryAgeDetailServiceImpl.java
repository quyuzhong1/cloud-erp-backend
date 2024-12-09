package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OverseasInventoryAgeDetailDTO;
import com.erp.model.wms.entity.OverseasInventoryAgeDetailEntity;
import com.erp.server.wms.mapper.OverseasInventoryAgeDetailMapper;
import com.erp.server.wms.service.OverseasInventoryAgeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 海外仓库存库龄明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2024-12-06
 */
@Slf4j
@Service
public class OverseasInventoryAgeDetailServiceImpl extends SuperServiceImpl<OverseasInventoryAgeDetailMapper, OverseasInventoryAgeDetailEntity> implements OverseasInventoryAgeDetailService {

    @Override
    public List<OverseasInventoryAgeDetailDTO.AgeRangeViewDTO> getAgeRangeViewByMainIds(List<String> mainIds) {
        if(CollUtil.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return this.baseMapper.getAgeRangeViewByMainIds(mainIds, LocalDate.now());
    }

    @Override
    public PagingVO<OverseasInventoryAgeDetailDTO.ListDTO> pagingSelect(PagingDTO<OverseasInventoryAgeDetailDTO.PagingParamDTO> dto) {
        OverseasInventoryAgeDetailDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasInventoryAgeDetailDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())){
            new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }
}
