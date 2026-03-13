package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.CfgThirdWarehouseOperationDescriptionValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.CfgThirdWarehouseOperationDescriptionValueDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2026-03-13
 */
@Mapper
public interface CfgThirdWarehouseOperationDescriptionValueMapper extends BaseMapper<CfgThirdWarehouseOperationDescriptionValueEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgThirdWarehouseOperationDescriptionValueDTO.ListDTO> paging(Page query, @Param("params") CfgThirdWarehouseOperationDescriptionValueDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgThirdWarehouseOperationDescriptionValueDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgThirdWarehouseOperationDescriptionValueDTO.ListDTO> listExport(@Param("params") CfgThirdWarehouseOperationDescriptionValueDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgThirdWarehouseOperationDescriptionValueDTO.TabListDTO> tabList(@Param("params") CfgThirdWarehouseOperationDescriptionValueDTO.PagingParamDTO searchParam);
}
