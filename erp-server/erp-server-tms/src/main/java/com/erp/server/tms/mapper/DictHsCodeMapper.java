package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.DictInvoiceHsDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.tms.dto.DictHsCodeDTO;
import com.erp.model.tms.entity.DictHsCodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 出口申报要素表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-07-11
 */
@Mapper
public interface DictHsCodeMapper extends BaseMapper<DictHsCodeEntity> {

    IPage<DictHsCodeDTO.ListDTO> paging(Page query, @Param("params")  DictHsCodeDTO. PagingParamDTO params);

    List<DictHsCodeDTO.SearchDTO> searchByKey(@Param("params")  DictHsCodeDTO.SearchParamDTO params);

    IPage<DictHsCodeDTO.ListBRDTO> pagingByBR(Page query,@Param("params") DictHsCodeDTO.PagingParamDTO params);
}
