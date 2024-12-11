package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.vo.PagingVO;
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

    /**
     * 下单跟踪明细
     * @param page 分页参数
     * @param params 参数
     */
    PagingVO<MouldInfoDTO.OrderTrackingDetailDTO> orderTrackingDetail(@Param("page") Page<MouldInfoDTO.OrderTrackingDetailDTO> page,
                                                                      @Param("params") MouldInfoDTO.OrderTrackingDetailParamDTO params);

    /**
     * 下单跟踪合计
     * @param params 参数
     */
    MouldInfoDTO.OrderTrackingTotalDTO orderTrackingTotal(@Param("params") MouldInfoDTO.PagingParamDTO params);

    /**
     * 下单跟踪明细合计
     * @param dto 参数
     */
    MouldInfoDTO.OrderTrackingDetailTotalDTO orderTrackingDetailTotal(MouldInfoDTO.OrderTrackingDetailParamDTO dto);

}
