package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.PurchaseSuggestIndependentDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
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
public interface PurchaseSuggestIndependentMapper extends BaseMapper<PurchaseSuggestMergeEntity> {

    /**
     * 分页查询
     * @author will
     * @date 2024/10/16 10:26
     * @param query
     * @param params 
     * @return IPage<ListDTO>
     */
    IPage<PurchaseSuggestIndependentDTO.ListDTO> paging(Page query, @Param("params") PurchaseSuggestIndependentDTO.PagingParamDTO params);

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
    List<PurchaseSuggestIndependentDTO.ListDTO> list(@Param("params") PurchaseSuggestIndependentDTO.ListParamDTO params);
}
