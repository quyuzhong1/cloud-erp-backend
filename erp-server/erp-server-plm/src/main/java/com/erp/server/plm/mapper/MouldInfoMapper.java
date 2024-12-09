package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.MouldInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 模具主表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Mapper
public interface MouldInfoMapper extends BaseMapper<MouldInfoEntity> {

    /**
     * 分页
     * @param page 分页参数
     * @param params 参数
     */
    Page<MouldInfoDTO.PagingViewDTO> paging(@Param("page") Page<MouldInfoDTO.PagingViewDTO> page, @Param("params") MouldInfoDTO.PagingParamDTO params);

    /**
     * 下单跟踪
     * @param page 分页参数
     * @param params 参数
     */
    Page<MouldInfoDTO.OrderTrackingViewDTO> orderTracking(@Param("page") Page<MouldInfoDTO.OrderTrackingViewDTO> page, @Param("params") MouldInfoDTO.PagingParamDTO params);
}
