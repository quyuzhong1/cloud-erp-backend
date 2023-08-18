package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.SoB2cEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.SoB2cDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * B2C销售订单表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cMapper extends BaseMapper<SoB2cEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SoB2cDTO.ListDTO> paging(Page query, @Param("params") SoB2cDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SoB2cDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SoB2cDTO.ListDTO> listExport(@Param("params") SoB2cDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SoB2cDTO.TabListDTO> tabList(@Param("params") SoB2cDTO.PagingParamDTO searchParam);
}
