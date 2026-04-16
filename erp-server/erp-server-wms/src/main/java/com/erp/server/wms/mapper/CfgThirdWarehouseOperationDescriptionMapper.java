package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.CfgThirdWarehouseOperationDescriptionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.CfgThirdWarehouseOperationDescriptionDTO;
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
public interface CfgThirdWarehouseOperationDescriptionMapper extends BaseMapper<CfgThirdWarehouseOperationDescriptionEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgThirdWarehouseOperationDescriptionDTO.ListDTO> paging(Page query, @Param("params") CfgThirdWarehouseOperationDescriptionDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgThirdWarehouseOperationDescriptionDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgThirdWarehouseOperationDescriptionDTO.ListDTO> listExport(@Param("params") CfgThirdWarehouseOperationDescriptionDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgThirdWarehouseOperationDescriptionDTO.TabListDTO> tabList(@Param("params") CfgThirdWarehouseOperationDescriptionDTO.PagingParamDTO searchParam);
}
