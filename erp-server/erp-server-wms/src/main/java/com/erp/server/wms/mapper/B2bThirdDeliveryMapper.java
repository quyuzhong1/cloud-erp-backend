package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2B三方发货单 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Mapper
public interface B2bThirdDeliveryMapper extends BaseMapper<B2bThirdDeliveryEntity> {

    List<B2bThirdDeliveryDTO.TabListDTO> tabList();

    IPage<B2bThirdDeliveryDTO.PagingViewDTO> paging(@Param("query") Page<B2bThirdDeliveryDTO.PagingViewDTO> query, @Param("params") B2bThirdDeliveryDTO.PagingParamDTO params);
}
