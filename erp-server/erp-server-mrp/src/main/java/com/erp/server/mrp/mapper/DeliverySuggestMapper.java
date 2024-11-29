package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
     * 分页查询
     * @author will
     * @date 2024/10/16 10:37
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<DeliverySuggestDTO.ListDTO> paging(Page query,@Param("params") DeliverySuggestDTO.PagingParamDTO params);
    /**
     * 查询发货建议导出数据
     * @author will
     * @date 2024/10/12 15:06
     * @param page
     * @param params
     * @return Page<DeliverySuggestionDTO>
     */
    Page<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> pagingExportDeliverySuggestion(Page<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> page, @Param("params") ReplenishmentSuggestionDTO.PagingParamDTO params);
    /**
     * 查询需要已完成超过三十天的数据
     * @author will
     * @date 2024/11/12 12:04
     * @return List<DeliverySuggestEntity>
     */
    List<DeliverySuggestEntity> listFinishDeliverySuggest(@Param("finishDate") LocalDate finishDate);

}
