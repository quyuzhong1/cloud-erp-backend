package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.ProductRefBuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.ProductRefBuDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 产品bu信息关联表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
@Mapper
public interface ProductRefBuMapper extends BaseMapper<ProductRefBuEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<ProductRefBuDTO.ListDTO> paging(Page query, @Param("params") ProductRefBuDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") ProductRefBuDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<ProductRefBuDTO.ListDTO> listExport(@Param("params") ProductRefBuDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<ProductRefBuDTO.TabListDTO> tabList(@Param("params") ProductRefBuDTO.PagingParamDTO searchParam);
}
