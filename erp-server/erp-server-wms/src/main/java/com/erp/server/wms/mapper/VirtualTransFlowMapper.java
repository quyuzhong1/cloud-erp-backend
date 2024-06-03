package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 虚拟库存交易流水表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Mapper
public interface VirtualTransFlowMapper extends BaseMapper<VirtualTransFlowEntity> {
    /**
     * 虚拟库存流水分页查询
     * @author will
     * @date 2024/6/3 17:10
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualTransFlowDTO.ListDTO> paging(Page query,@Param("params") VirtualTransFlowDTO.SearchParamDTO params);
}
