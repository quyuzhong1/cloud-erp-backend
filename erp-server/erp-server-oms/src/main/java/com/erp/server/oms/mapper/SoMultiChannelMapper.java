package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 多渠道订单主表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
 */
@Mapper
public interface SoMultiChannelMapper extends BaseMapper<SoMultiChannelEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SoMultiChannelDTO.ListDTO> paging(Page query, @Param("params") SoMultiChannelDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SoMultiChannelDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SoMultiChannelDTO.ListDTO> listExport(@Param("params") SoMultiChannelDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SoMultiChannelDTO.TabListDTO> tabList(@Param("params") SoMultiChannelDTO.PagingParamDTO searchParam);

    /**
     * 根据订单ID查询多渠道订单
     * @param ids
     * @return
     */
    List<SoMultiChannelDTO.SoViewDTO> listSoMultiChannelBySoId(@Param("ids") List<String> ids);

    SoMultiChannelEntity getBySoId(@Param("soId") String soId);

    List<SoMultiChannelEntity> queryMultiChannelDeliveryStatus();
}
