package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.entity.TmsCarrierEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 承运商 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2024-07-04
 */
@Mapper
public interface TmsCarrierMapper extends BaseMapper<TmsCarrierEntity> {

    IPage<BaseDropDownDTO.CommonDTO> pagingSelect(@Param("query") Page query, @Param("params") LogisticsSaleChannelDTO.SelectDTO params);
}
