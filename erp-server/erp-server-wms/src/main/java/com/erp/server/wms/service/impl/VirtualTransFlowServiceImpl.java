package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.server.wms.mapper.VirtualTransFlowMapper;
import com.erp.server.wms.service.VirtualTransFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * <p>
 * 虚拟库存交易流水表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualTransFlowServiceImpl extends SuperServiceImpl<VirtualTransFlowMapper, VirtualTransFlowEntity> implements VirtualTransFlowService {

    @Override
    public PagingVO<VirtualTransFlowDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/6/3 17:10
     * @param list
     */
    private void fillPageData (List<VirtualTransFlowDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
    }
}
