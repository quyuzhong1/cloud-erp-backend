package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.BasicProductBuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.BasicProductBuDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 产品BU信息 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
@Mapper
public interface BasicProductBuMapper extends BaseMapper<BasicProductBuEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<BasicProductBuDTO.ListDTO> paging(Page query, @Param("params") BasicProductBuDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") BasicProductBuDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<BasicProductBuDTO.ListDTO> listExport(@Param("params") BasicProductBuDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<BasicProductBuDTO.TabListDTO> tabList(@Param("params") BasicProductBuDTO.PagingParamDTO searchParam);
}
