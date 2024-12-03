package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 虚拟仓库明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualInventoryDetailMapper extends BaseMapper<VirtualInventoryDetailEntity> {
    /**
     * 分页列表
     * @author will
     * @date 2024/12/3 17:41
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualInventoryAgeDTO.ListDTO> paging(Page<VirtualInventoryAgeDTO.SearchParamDTO> page,@Param("params") VirtualInventoryAgeDTO.SearchParamDTO params);
}
