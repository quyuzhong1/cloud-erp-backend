package com.erp.server.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 采购对账单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Mapper
public interface PoReconciliationMapper extends BaseMapper<PoReconciliationEntity> {
    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 12:13
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PoReconciliationDTO.ListDTO> paging(Page query,@Param("params") PoReconciliationDTO.PagingParamDTO params);
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:22
     * @param params
     * @return List<ListDTO>
     */
    List<PoReconciliationDTO.ListDTO> listExport(@Param("params") PoReconciliationDTO.PagingParamDTO params);
    Page<PoReconciliationDTO.ListDTO> listExport(@Param("page") Page<PoReconciliationDTO.ListDTO> page, @Param("params") PoReconciliationDTO.PagingParamDTO params);
    /**
     * @description: 获取列表统计
     * @author Will
     * @date: 2024/1/23 16:09
     * @param searchParam
     * @return Integer
     */
    Integer tabList(@Param("params") PoReconciliationDTO.PagingParamDTO searchParam);
    /**
     * @description: 查询待供方确认
     * @author Will
     * @date: 2024/1/27 15:20
     * @return List<AddPoReconciliationViewDTO>
     */
    List<PoReconciliationDetailDTO.AddPoReconciliationViewDTO> viewToBeSupplierConfirm(@Param("params")PermissionsDTO dto);
    /**
     * 查询所有的对账单明细
     * @author will
     * @date 2025/8/12 18:03
     * @param query
     * @param params
     * @return IPage<ExportDetailDTO>
     */
    IPage<PoReconciliationDTO.ExportDetailDTO> exportAllPoReconciliationDetail(Page query,@Param("params") PoReconciliationDTO.PagingParamDTO params);
}
