package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.WarehouseLocationMoveInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.WarehouseLocationMoveInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 仓位移动主表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
@Mapper
public interface WarehouseLocationMoveInfoMapper extends BaseMapper<WarehouseLocationMoveInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<WarehouseLocationMoveInfoDTO.ListDTO> paging(Page query, @Param("params") WarehouseLocationMoveInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") WarehouseLocationMoveInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<WarehouseLocationMoveInfoDTO.ListDTO> listExport(@Param("params") WarehouseLocationMoveInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<WarehouseLocationMoveInfoDTO.TabListDTO> tabList(@Param("params") WarehouseLocationMoveInfoDTO.PagingParamDTO searchParam);
}
