package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.CfgQcUserDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 质检员配置 Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2026-05-27
 */
@Mapper
public interface CfgQcUserMapper extends BaseMapper<CfgQcUserEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgQcUserDTO.ListDTO> paging(Page query, @Param("params") CfgQcUserDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgQcUserDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgQcUserDTO.ListDTO> listExport(@Param("params") CfgQcUserDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgQcUserDTO.TabListDTO> tabList(@Param("params") CfgQcUserDTO.PagingParamDTO searchParam);

    /**
     * 根据供应商ID和仓库ID查询配置（精确匹配）
     *
     * @param supplierId  供应商ID
     * @param warehouseId 仓库ID
     * @return 质检员配置
     */
    CfgQcUserEntity selectBySupplierIdAndWarehouseId(@Param("supplierId") String supplierId,
                                                     @Param("warehouseId") String warehouseId);

    /**
     * 按 (supplierId, warehouseId) 组合批量查询配置（用于导入批处理，避免 N+1 与笛卡尔积误匹配）
     *
     * @param pairs 供应商 + 仓库组合列表
     * @return 质检员配置列表
     */
    List<CfgQcUserEntity> selectBySupplierWarehousePairs(@Param("pairs") List<CfgQcUserDTO.SupplierWarehousePair> pairs);
}
