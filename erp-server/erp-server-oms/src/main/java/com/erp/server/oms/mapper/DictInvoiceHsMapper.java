package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.DictInvoiceHsDTO;
import com.erp.model.oms.entity.DictInvoiceHsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 发票海关编码 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Mapper
public interface DictInvoiceHsMapper extends BaseMapper<DictInvoiceHsEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2025/4/8 16:51
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<DictInvoiceHsDTO.ListDTO> paging(Page query, @Param("params") DictInvoiceHsDTO.PagingParamDTO params);
}
