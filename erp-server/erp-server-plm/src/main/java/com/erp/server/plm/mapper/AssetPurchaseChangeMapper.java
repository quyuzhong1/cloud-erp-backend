package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.AssetPurchaseChangeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.AssetPurchaseChangeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Mapper
public interface AssetPurchaseChangeMapper extends BaseMapper<AssetPurchaseChangeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetPurchaseChangeDTO.ListDTO> paging(Page query, @Param("params") AssetPurchaseChangeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetPurchaseChangeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AssetPurchaseChangeDTO.ListDTO> listExport(@Param("params") AssetPurchaseChangeDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetPurchaseChangeDTO.TabListDTO> tabList(@Param("params") AssetPurchaseChangeDTO.PagingParamDTO searchParam);
}
