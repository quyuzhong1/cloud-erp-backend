package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PushSyncStatusDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 供应商表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface SupplierMapper extends BaseMapper<SupplierEntity> {

    IPage<SupplierDTO.PagingViewDTO> paging(Page query, @Param("params") SupplierDTO.PagingParamDTO params);

    List<SupplierDTO.PagingViewDTO> getExportSupplier(@Param("params") SupplierDTO.ExportDTO dto);

    List<SupplierDTO.SupplierSimpleDTO> listSupplierByCategoryType(@Param("type") String supplierCategory, @Param("value")String categoryType,
                                               @Param("approveStatus")String approveStatus);

    /**
     * @description: 更新金蝶推送状态
     * @author Will
     * @date: 2023/9/26 18:34
     * @param kingdeeDTO
     */
    void updateSyncKingdeeStatus(@Param("params")PushSyncStatusDTO.KingdeeDTO kingdeeDTO);
}
