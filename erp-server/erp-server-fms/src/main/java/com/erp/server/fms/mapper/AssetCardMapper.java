package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetCardEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetCardDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 资产卡片主表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetCardMapper extends BaseMapper<AssetCardEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetCardDTO.ListDTO> paging(Page query, @Param("params") AssetCardDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetCardDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    IPage<AssetCardDTO.ListDTO> listExport(Page page,@Param("params") AssetCardDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetCardDTO.TabListDTO> tabList(@Param("params") AssetCardDTO.PagingParamDTO searchParam);

    /**
    * 获取已审核资产卡片列表（用于盘点方案）
    * @param params
    * @return
    */
    List<AssetCardDTO.ApprovedCardDTO> getApprovedCardList(@Param("params") AssetCardDTO.QueryApprovedDTO params);
}
