package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


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
     * 采购建议分页导出
     * @author will
     * @date 2024/9/6 15:27
     * @param page
     * @param params
     * @return Page<PurchaseSuggestionDTO>
     */
    Page<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> pagingExportPurchaseSuggestion(Page page, @Param("params") ReplenishmentSuggestionDTO.PagingParamDTO params);
}
