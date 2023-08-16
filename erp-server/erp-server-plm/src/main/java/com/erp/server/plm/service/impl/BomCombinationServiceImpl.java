package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.BomCombinationDTO;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.BomCombinationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品业务层
 * @date 2023/8/16 10:01
 */
@Service
public class BomCombinationServiceImpl implements BomCombinationService {

    @Resource
    private BomInfoMapper bomInfoMapper;

    @Override
    public PagingVO<BomCombinationDTO.ListDTO> paging(PagingDTO<BomCombinationDTO.SearchParamDTO> dto) {
        BomCombinationDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<BomCombinationDTO.ListDTO> pageData = bomInfoMapper.combinationPaging(query, dto);
        //处理分页数据
        handlePaging(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void handlePaging (List<BomCombinationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        for (BomCombinationDTO.ListDTO dto : records) {
            if (CollectionUtils.isNotEmpty(dto.getChildList())) {
                String collect = dto.getChildList().stream().map(BomCombinationDTO.ChildDTO::getChildSkuNo).collect(Collectors.joining(","));
            }
        }

    }

    @Override
    public Boolean add(BomCombinationDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(BomCombinationDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean view(BaseIdDTO dto) {
        return null;
    }
}
