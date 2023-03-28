package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 供应商升降级 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface SupplierPhaseMapper extends BaseMapper<SupplierPhaseEntity> {

    IPage<SupplierPhaseDTO.PagingViewDTO> paging(Page query, @Param("params") SupplierPhaseDTO.PagingParamDTO params,@Param("supplierPhaseIdList") List<String> supplierPhaseIdList);
}
