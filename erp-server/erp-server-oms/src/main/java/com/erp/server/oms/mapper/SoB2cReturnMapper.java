package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * b2c退货订单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
@Mapper
public interface SoB2cReturnMapper extends BaseMapper<SoB2cReturnEntity> {

    IPage<SoB2cReturnDTO.PagingViewDTO> paging(Page query, SoB2cReturnDTO.PagingParamDTO params);
}
