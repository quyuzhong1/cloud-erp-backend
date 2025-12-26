package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.KolB2bApplicationDTO;
import com.erp.model.oms.entity.KolB2bApplicationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * B2B寄样申请主表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Mapper
public interface KolB2bApplicationMapper extends BaseMapper<KolB2bApplicationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<KolB2bApplicationDTO.ListDTO> paging(Page query, @Param("params") KolB2bApplicationDTO.PagingParamDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    Integer tabList(@Param("params") KolB2bApplicationDTO.PagingParamDTO searchParam);

    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    Integer tabDetailList(@Param("params") KolB2bApplicationDTO.PagingParamDTO searchParam);
}
