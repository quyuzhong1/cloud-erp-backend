package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.KolB2cApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.KolB2cApplicationDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * B2C寄样申请单 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Mapper
public interface KolB2cApplicationMapper extends BaseMapper<KolB2cApplicationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<KolB2cApplicationDTO.ListDTO> paging(Page query, @Param("params") KolB2cApplicationDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") KolB2cApplicationDTO.PagingParamDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<KolB2cApplicationDTO.TabListDTO> tabList(@Param("params") KolB2cApplicationDTO.PagingParamDTO searchParam);
}
