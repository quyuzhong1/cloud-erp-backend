package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 供应商表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface SupplierMapper extends BaseMapper<SupplierEntity> {

    IPage<SupplierDTO.PagingViewDTO> paging(Page query, @Param("params") SupplierDTO.PagingParamDTO params);

    Page<SupplierDTO.PagingExportDTO> getExportSupplier(@Param("page") Page<SupplierDTO.PagingViewDTO> page, @Param("params") SupplierDTO.PagingParamDTO dto);

    List<SupplierDTO.SupplierSimpleDTO> listSupplierByCategoryType(@Param("type") String supplierCategory, @Param("value")String categoryType,
                                               @Param("approveStatus")String approveStatus);

    IPage<BaseDropDownDTO.RemarkDTO> pagingSelect(Page query, @Param("params")BaseDropDownDTO.SelectDTO params);
}
