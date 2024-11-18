package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 发货计划 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Mapper
public interface DeliverySuggestMapper extends BaseMapper<DeliverySuggestEntity> {
    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 14:14
     * @param params
     * @return List<ListDTO>
     */
    List<DeliverySuggestDTO.ListDTO> list(@Param("params") DeliverySuggestDTO.ListParamDTO params);
    /**
     * 查询发货建议导出数据
     * @author will
     * @date 2024/10/12 15:06
     * @param page
     * @param params
     * @return Page<DeliverySuggestionDTO>
     */
    Page<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> pagingExportDeliverySuggestion(Page<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> page, @Param("params") ReplenishmentSuggestionDTO.PagingParamDTO params);
}
