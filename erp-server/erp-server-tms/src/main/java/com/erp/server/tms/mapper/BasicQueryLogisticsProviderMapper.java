package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.BasicQueryLogisticsProviderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.BasicQueryLogisticsProviderDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 查询物流商信息表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-03-31
 */
@Mapper
public interface BasicQueryLogisticsProviderMapper extends BaseMapper<BasicQueryLogisticsProviderEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<BasicQueryLogisticsProviderDTO.ListDTO> paging(Page query, @Param("params") BasicQueryLogisticsProviderDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") BasicQueryLogisticsProviderDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<BasicQueryLogisticsProviderDTO.ListDTO> listExport(@Param("params") BasicQueryLogisticsProviderDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<BasicQueryLogisticsProviderDTO.TabListDTO> tabList(@Param("params") BasicQueryLogisticsProviderDTO.PagingParamDTO searchParam);
}
