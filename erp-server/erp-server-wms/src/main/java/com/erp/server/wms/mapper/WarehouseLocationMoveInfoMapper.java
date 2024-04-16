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
    * Pda:分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<WarehouseLocationMoveInfoDTO.PdaListDTO> pdaPaging(Page query, @Param("params") WarehouseLocationMoveInfoDTO.PagingParamDTO params);

    /**
     * 列表查询-pc端
     * @author hyj
     * @date 2024/4/12 16:46
     * @param params
     * @return
     */
    IPage<WarehouseLocationMoveInfoDTO.PdaPcListDTO> pdaPcPaging(Page query, @Param("params") WarehouseLocationMoveInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    Integer listCount(@Param("params") WarehouseLocationMoveInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<WarehouseLocationMoveInfoDTO.ListDTO> listExport(@Param("params") WarehouseLocationMoveInfoDTO.ExportDTO params);
}
