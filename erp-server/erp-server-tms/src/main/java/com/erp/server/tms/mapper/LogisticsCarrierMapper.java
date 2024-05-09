package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsCarrierDTO;
import com.erp.model.tms.entity.LogisticsCarrierEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流快递/海运/空运公司列表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
@Mapper
public interface LogisticsCarrierMapper extends BaseMapper<LogisticsCarrierEntity> {
    /**
     * 分页模糊搜素
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsCarrierDTO.PagingVO> dropDown(@Param("query") Page query, @Param("params") LogisticsCarrierDTO.SearchDTO params);
}
