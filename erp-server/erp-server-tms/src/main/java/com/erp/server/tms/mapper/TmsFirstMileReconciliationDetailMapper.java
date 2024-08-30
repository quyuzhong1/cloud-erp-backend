package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 头程对账单明细 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Mapper
public interface TmsFirstMileReconciliationDetailMapper extends BaseMapper<TmsFirstMileReconciliationDetailEntity> {

    IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(Page<?> query, @Param("params") TmsFirstMileReconciliationDetailDTO.PagingParamDTO params);

    List<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> listExport(@Param("params") TmsFirstMileReconciliationDetailDTO.ExportDTO param);
    Page<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> listExport(@Param("page") Page<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> page,@Param("params") TmsFirstMileReconciliationDetailDTO.ExportDTO param);

    String getCurrencyById(@Param("id") String id);

    List<TmsFirstMileReconciliationDetailEntity> listByMainIdsBySort(@Param("mainIds") List<String> mainIds);
}
