package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购价变更表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface PurchasePriceChangeMapper extends BaseMapper<PurchasePriceChangeEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/10/18 17:21
     * @param query
     * @param params
     * @return IPage<PagingViewDTO>
     */
    IPage<PurchasePriceChangeDTO.PagingViewDTO> paging(Page query,@Param("params") PurchasePriceChangeDTO.PagingParamDTO params);
    /**
     * @description: 查询导出
     * @author Will
     * @date: 2023/10/18 17:21
     * @param dto
     * @return List<PagingViewDTO>
     */
    List<PurchasePriceChangeDTO.PagingViewDTO> listExport(@Param("params") PurchasePriceChangeDTO.PagingParamDTO dto);
    Page<PurchasePriceChangeDTO.PagingViewDTO> listExport(@Param("page") Page<PurchasePriceChangeDTO.PagingViewDTO> page, @Param("params") PurchasePriceChangeDTO.PagingParamDTO dto);

    /**
     * 临时查询方法
     * @returnch
     */
    List<PurchasePriceChangeDetailEntity> listTemp();
    /**
     * @description: tab集合
     * @author Will
     * @date: 2024/1/20 9:26
     * @param searchParamDTO
     * @return Integer
     */
    Integer tabList(@Param("params")PurchaseOrderDTO.SearchParamDTO searchParamDTO);
}
