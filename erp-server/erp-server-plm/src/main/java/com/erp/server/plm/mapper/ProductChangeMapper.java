package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.ProductChangeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 产品变更信息表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
@Mapper
public interface ProductChangeMapper extends BaseMapper<ProductChangeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<ProductChangeDTO.ListDTO> paging(Page query, @Param("params") ProductChangeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") ProductChangeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<ProductChangeDTO.ListDTO> listExport(@Param("params") ProductChangeDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<ProductChangeDTO.TabListDTO> tabList(@Param("params") ProductChangeDTO.PagingParamDTO searchParam);
}
