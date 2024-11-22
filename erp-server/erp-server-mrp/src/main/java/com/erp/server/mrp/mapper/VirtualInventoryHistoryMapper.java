package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.VirtualInventoryHistoryDTO;
import com.erp.model.mrp.entity.VirtualInventoryHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 虚拟库存表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-09-27
 */
@Mapper
public interface VirtualInventoryHistoryMapper extends BaseMapper<VirtualInventoryHistoryEntity> {
    /**
     * 根据SKU和虚拟仓库查询
     *
     * @param skuIdList              查询条件
     * @param virtualWarehouseIdList 参数
     * @param billDate
     */
    List<VirtualInventoryHistoryDTO.ListInventoryDTO> listVirtualWarehouseIdListAndSkuIdList(List<String> skuIdList, List<String> virtualWarehouseIdList, LocalDate billDate);

    /**
     * 库存分页
     * @param query  查询
     * @param params 参数
     */
    IPage<VirtualInventoryHistoryDTO.ListDTO> paging(Page<VirtualInventoryHistoryDTO.ListDTO> query, VirtualInventoryHistoryDTO.SearchParamDTO params);
}
