package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 售后装箱明细表 Mapper 接口
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Mapper
public interface AfterSalePackDetailMapper extends BaseMapper<AfterSalePackDetailEntity> {

    /**
     * 分页查询
     *
     * @param query
     * @param params
     * @return
     */
    IPage<AfterSalePackDetailDTO.ListDTO> paging(Page query, @Param("params") AfterSalePackDetailDTO.PagingParamDTO params);

}
