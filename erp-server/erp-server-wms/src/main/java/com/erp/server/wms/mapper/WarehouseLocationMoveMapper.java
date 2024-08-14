package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface WarehouseLocationMoveMapper extends BaseMapper<WarehouseLocationMoveEntity> {

    /**
    * Pda:分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<WarehouseLocationMoveDTO.PdaListDTO> pdaPaging(Page query, @Param("params") WarehouseLocationMoveDTO.PagingParamDTO params);

    /**
     * 列表查询-pc端
     * @author hyj
     * @date 2024/4/12 16:46
     * @param params
     * @return
     */
    IPage<WarehouseLocationMoveDTO.PdaPcListDTO> pdaPcPaging(Page query, @Param("params") WarehouseLocationMoveDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    Integer listCount(@Param("params") WarehouseLocationMoveDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<WarehouseLocationMoveDTO.PdaPcListDTO> listExport(@Param("params") WarehouseLocationMoveDTO.ExportDTO params);
    Page<WarehouseLocationMoveDTO.PdaPcListDTO> listExport(@Param("page") Page<WarehouseLocationMoveDTO.PdaPcListDTO> page, @Param("params") WarehouseLocationMoveDTO.ExportDTO params);
    /**
     * 展示详情-PC端
     * @param id
     * @return
     */
    List<WarehouseLocationMoveDTO.DetailViewDTO> getDetail(@Param("id") String id);
    /**
     * 展示详情-PC端
     * @param id
     * @return
     */
    WarehouseLocationMoveDTO.DetailViewDTO findOne(@Param("id") String id);
}
