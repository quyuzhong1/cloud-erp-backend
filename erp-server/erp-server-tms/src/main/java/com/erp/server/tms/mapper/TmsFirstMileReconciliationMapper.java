package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 头程对账单 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Mapper
public interface TmsFirstMileReconciliationMapper extends BaseMapper<TmsFirstMileReconciliationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<TmsFirstMileReconciliationDTO.ListDTO> paging(Page<?> query, @Param("params") TmsFirstMileReconciliationDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") TmsFirstMileReconciliationDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<TmsFirstMileReconciliationDTO.ListDTO> listExport(@Param("params") TmsFirstMileReconciliationDTO.ExportDTO params);
    Page<TmsFirstMileReconciliationDTO.ListDTO> listExport(@Param("page") Page<TmsFirstMileReconciliationDTO.ListDTO> page, @Param("params") TmsFirstMileReconciliationDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<TmsFirstMileReconciliationDTO.TabListDTO> tabList(@Param("params") TmsFirstMileReconciliationDTO.PagingParamDTO searchParam);
}
