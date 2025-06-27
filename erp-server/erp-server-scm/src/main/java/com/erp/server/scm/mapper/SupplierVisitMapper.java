package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.scm.entity.SupplierVisitEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 供应商拜访表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface SupplierVisitMapper extends BaseMapper<SupplierVisitEntity> {

    /**
     * 获取供应商拜访分页信息
     * @author yl
     * @date 2023-03-21 11:37
     * @param query
     * @param supplierId
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     */
    IPage<SupplierVisitDTO.PagingViewDTO> paging(Page query, @Param("supplierId") String supplierId);

    List<SupplierVisitDTO.TabListDTO> tabList(@Param("params")  SupplierCredentialDTO.PagingParamDTO params);

    IPage<SupplierVisitDTO.ListDTO> pagingList(Page query,@Param("params") SupplierVisitDTO.PagingParamDTO params);
}
