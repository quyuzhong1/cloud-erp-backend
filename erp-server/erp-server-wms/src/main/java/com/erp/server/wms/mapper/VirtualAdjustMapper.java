package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.VirtualAdjustEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.VirtualAdjustDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 虚拟仓调整单主表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
 */
@Mapper
public interface VirtualAdjustMapper extends BaseMapper<VirtualAdjustEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<VirtualAdjustDTO.ListDTO> paging(Page query, @Param("params") VirtualAdjustDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<VirtualAdjustDTO.TabListDTO> tabList(@Param("params") VirtualAdjustDTO.PagingParamDTO searchParam);
}
