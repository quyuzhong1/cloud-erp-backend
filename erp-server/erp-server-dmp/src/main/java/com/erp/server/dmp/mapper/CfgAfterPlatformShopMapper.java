package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
 */
@Mapper
public interface CfgAfterPlatformShopMapper extends BaseMapper<CfgAfterPlatformShopEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgAfterPlatformShopDTO.ListDTO> paging(Page query, @Param("params") CfgAfterPlatformShopDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgAfterPlatformShopDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgAfterPlatformShopDTO.ListDTO> listExport(@Param("params") CfgAfterPlatformShopDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgAfterPlatformShopDTO.TabListDTO> tabList(@Param("params") CfgAfterPlatformShopDTO.PagingParamDTO searchParam);
}
