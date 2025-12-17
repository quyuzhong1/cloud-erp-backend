package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流-第三方渠道关系表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
 */
@Mapper
public interface LogisticsThirdChannelRefMapper extends BaseMapper<LogisticsThirdChannelRefEntity> {

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsThirdChannelRefDTO.PagingVO> paging(Page<Object> query, LogisticsThirdChannelRefDTO.PagingParamDTO params);

    List<LogisticsThirdChannelRefDTO.PagingVO> listByPlatform(@Param("platformType") String platformType);
}
