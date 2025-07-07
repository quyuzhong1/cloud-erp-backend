package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.SupplierCredentialEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 供应商资质表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface SupplierCredentialMapper extends BaseMapper<SupplierCredentialEntity> {

    List<SupplierCredentialDTO.TabListDTO> tabList(@Param("params") SupplierCredentialDTO.PagingParamDTO params);

    IPage<SupplierCredentialDTO.ListDTO> paging(Page query,@Param("params") SupplierCredentialDTO.PagingParamDTO params);
}
