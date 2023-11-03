package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 物流地址表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsAddressMapper extends BaseMapper<LogisticsAddressEntity> {

    IPage<LogisticsAddressDTO.PagingViewDTO> paging(Page query,@Param("params") LogisticsAddressDTO.PagingParamDTO params);
}
