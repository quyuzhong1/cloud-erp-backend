package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.ReplenishmentSuggestionVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 补货建议主表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface ReplenishmentSuggestionMapper extends BaseMapper<ReplenishmentSuggestionEntity> {

    Page<ReplenishmentSuggestionVO.PagingView> paging(Page<Object> objectPage, ReplenishmentSuggestionDTO.PagingParamDTO params);

    ReplenishmentSuggestionVO.View view(String detailId);
}
