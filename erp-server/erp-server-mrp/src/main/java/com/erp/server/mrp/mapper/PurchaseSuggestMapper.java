package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 建议采购 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Mapper
public interface PurchaseSuggestMapper extends BaseMapper<PurchaseSuggestEntity> {

    /**
     * 分页查询
     * @author will
     * @date 2024/10/16 10:26
     * @param query
     * @param params 
     * @return IPage<ListDTO>
     */
    IPage<PurchaseSuggestDTO.ListDTO> paging(Page query,@Param("params") PurchaseSuggestDTO.PagingParamDTO params);

    /**
     * 采购建议分页导出
     * @author will
     * @date 2024/9/6 15:27
     * @param page
     * @param params
     * @return Page<PurchaseSuggestionDTO>
     */
    Page<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> pagingExportPurchaseSuggestion(Page<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> page, @Param("params") ReplenishmentSuggestionDTO.PagingParamDTO params);
    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:49
     * @param params
     * @return List<ListDTO>
     */
    List<PurchaseSuggestDTO.ListDTO> list(@Param("params") PurchaseSuggestDTO.ListParamDTO params);
    /**
     * 查询可生成采购建议合并的数据
     * @author will
     * @date 2024/10/31 9:46
     * @return List<PurchaseSuggestEntity>
     */
    List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(@Param("entity") PurchaseSuggestEntity purchaseSuggestEntity);
}
