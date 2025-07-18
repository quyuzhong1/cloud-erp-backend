package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 三方仓发货单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Mapper
public interface ThirdWarehouseDeliveryMapper extends BaseMapper<ThirdWarehouseDeliveryEntity> {

    IPage<ThirdWarehouseDeliveryDTO.PagingViewDTO> paging(Page query, @Param("params") ThirdWarehouseDeliveryDTO.PagingParamDTO params);
}
