package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 采购价变更表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface PurchasePriceChangeMapper extends BaseMapper<PurchasePriceChangeEntity> {

    IPage<PurchasePriceChangeDTO.PagingViewDTO> paging(Page query,@Param("params") PurchasePriceChangeDTO.PagingParamDTO params);
}
