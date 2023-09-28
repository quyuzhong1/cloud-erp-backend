package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PushSyncStatusDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购价目表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface PurchasePriceMapper extends BaseMapper<PurchasePriceEntity> {

    IPage<PurchasePriceDTO.PagingViewDTO> paging(Page query,@Param("params") PurchasePriceDTO.PagingParamDTO params,@Param("statusList") List<String> statusList);

    /**
     * 获取导出数据
     * @author yl
     * @date 2023-03-27 17:57
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDTO.PagingViewDTO>
     */
    List<PurchasePriceDTO.PagingViewDTO> getExport(@Param("params") PurchasePriceDTO.ExportDTO dto);

    /**
     * 获取供应商价格
     * @author yl
     * @date 2023-03-27 17:57
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDTO.PagingViewDTO>
     */
    List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(@Param("ids") List<String> ids);

    /**
     * @description: 更新金蝶推送状态
     * @author Will
     * @date: 2023/9/26 18:34
     * @param kingdeeDTO
     */
    void updateSyncKingdeeStatus(@Param("params")PushSyncStatusDTO.KingdeeDTO kingdeeDTO);
}
