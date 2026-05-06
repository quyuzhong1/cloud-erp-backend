package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2bDeliveryInterceptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * b2b发货拦截单 Mapper 接口
 * </p>
 *
 * @author Codex
 */
@Mapper
public interface SoB2bDeliveryInterceptMapper extends BaseMapper<SoB2bDeliveryInterceptEntity> {

    List<SoB2bDeliveryInterceptDTO.TabListDTO> tabList(@Param("params") SoB2bDeliveryInterceptDTO.PagingParamDTO params);

    IPage<SoB2bDeliveryInterceptDTO.ListDTO> paging(Page<?> query, @Param("params") SoB2bDeliveryInterceptDTO.PagingParamDTO params);
}
