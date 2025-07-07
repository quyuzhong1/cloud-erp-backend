package com.erp.server.scm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * <p>
 * 销量设置 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
@Mapper
public interface CfgSupplierSalesMapper extends BaseMapper<CfgSupplierSalesEntity> {

    IPage<CfgSupplierSalesDTO.ListDTO> paging(Page query, @Param("params") CfgSupplierSalesDTO.PagingParamDTO params);
}
