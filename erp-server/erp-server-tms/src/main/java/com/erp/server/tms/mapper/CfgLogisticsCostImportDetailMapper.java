package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 费用项配置字段配置 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Mapper
public interface CfgLogisticsCostImportDetailMapper extends BaseMapper<CfgLogisticsCostImportDetailEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgLogisticsCostImportDetailDTO.ListDTO> paging(Page query, @Param("params") CfgLogisticsCostImportDetailDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgLogisticsCostImportDetailDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgLogisticsCostImportDetailDTO.ListDTO> listExport(@Param("params") CfgLogisticsCostImportDetailDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgLogisticsCostImportDetailDTO.TabListDTO> tabList(@Param("params") CfgLogisticsCostImportDetailDTO.PagingParamDTO searchParam);
}
