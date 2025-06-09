package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.LocalInTransitDetailEntity;
import com.erp.model.mrp.vo.LocalInTransitDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 本地在途明细 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface LocalInTransitDetailMapper extends BaseMapper<LocalInTransitDetailEntity> {

    Page<LocalInTransitDetailVO> localInTransitDetail(@Param("page") Page<LocalInTransitDetailVO> page, @Param("params") ReplenishmentSuggestionDTO.DetailParamDTO params);
}
