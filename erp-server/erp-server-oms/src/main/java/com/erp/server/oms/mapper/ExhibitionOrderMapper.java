package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.ExhibitionOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 展会订单信息 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Mapper
public interface ExhibitionOrderMapper extends BaseMapper<ExhibitionOrderEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<ExhibitionOrderDTO.ListDTO> paging(Page query, @Param("params") ExhibitionOrderDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") ExhibitionOrderDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<ExhibitionOrderDTO.ListDTO> listExport(@Param("params") ExhibitionOrderDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<ExhibitionOrderDTO.TabListDTO> tabList(@Param("params") ExhibitionOrderDTO.PagingParamDTO searchParam);
}
