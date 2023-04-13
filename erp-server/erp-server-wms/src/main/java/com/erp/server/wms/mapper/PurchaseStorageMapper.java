package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 采购入库单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Mapper
public interface PurchaseStorageMapper extends BaseMapper<PurchaseStockInEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/4/13 14:40
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseStockInDTO.ListDTO> paging(Page query,@Param("params") PurchaseStockInDTO.SearchParamDTO params);
    /**
     * @description: 列表查询数量
     * @author Will
     * @date: 2023/4/13 15:10
     * @param searchParamDTO
     * @return Integer
     */
    Integer listCount(@Param("params") PurchaseStockInDTO.SearchParamDTO searchParamDTO);
}
