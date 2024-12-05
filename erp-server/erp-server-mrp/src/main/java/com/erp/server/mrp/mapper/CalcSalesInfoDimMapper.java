package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 销量试算表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Mapper
public interface CalcSalesInfoDimMapper extends BaseMapper<CalcSalesInfoDimEntity> {


    /**
     * @param page   分页
     * @param params 参数
     */
    Page<CalcSalesInfoDimDTO.PagingView> paging(@Param("page") Page<CalcSalesInfoDimDTO.PagingView> page, @Param("params") CalcSalesInfoDimDTO.PagingParamDTO params);

    /**
     * @param id id
     */
    CalcSalesInfoDimDTO.ViewDTO view(String id);

    /**
     * 导出
     */
    Page<CalcSalesInfoDimDTO.ExportDTO> exportData(@Param("page") Page<CalcSalesInfoDimDTO.ExportDTO> page, @Param("params") CalcSalesInfoDimDTO.ExportSalesInfoDTO params);

    /**
     * @param page   分页
     * @param params 参数
     * @param uid    用户id
     */
    Page<CalcSalesInfoDimDTO.DetailViewDTO> pagingDetail(@Param("page") Page<CalcSalesInfoDimDTO.DetailViewDTO> page, @Param("params") CalcSalesInfoDimDTO.ParamDTO params, @Param("uid") String uid);

    /**
     * @param page   分页
     * @param params 参数
     * @param uid    用户id
     */
    Page<CalcSalesInfoDimDTO.TemplateViewDTO> pagingTemplate(@Param("page") Page<CalcSalesInfoDimDTO.TemplateViewDTO> page, @Param("params") CalcSalesInfoDimDTO.ParamDTO params, @Param("uid") String uid);

    /**
     * 根据sku店铺，试算开始时间查询数据
     * @param dto 参数
     */
    List<CalcSalesInfoDimDTO.CompareResultDTO> listBySkuAndShopAndDate(@Param("dto") CalcSalesInfoDimDTO.CalcCompareParamsDTO dto);

    /**
     * 通过id查询
     * @param ids id
     */
    List<CalcSalesInfoDimDTO.CompareResultDTO> listCompareByIds(@Param("ids") List<String> ids);
}
