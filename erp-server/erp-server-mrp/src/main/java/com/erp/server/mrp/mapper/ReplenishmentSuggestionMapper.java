package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.ReplenishmentSuggestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    /**
     * 根据唯一键查询（平台、店铺、sku）
     * @author will
     * @date 2024/9/3 16:37
     * @param platformCodeList
     * @param shopIdList
     * @param skuIdList
     * @return List<ReplenishmentSuggestionEntity>
     */
    List<ReplenishmentSuggestionEntity> listByUnique(@Param("platformCodeList") List<String> platformCodeList,@Param("shopIdList")  List<String> shopIdList,@Param("skuIdList")  List<String> skuIdList);
}
