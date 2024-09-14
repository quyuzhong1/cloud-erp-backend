package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.OverseasInTransitDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.vo.OverseasInTransitDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 海外在途明细 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface OverseasInTransitDetailMapper extends BaseMapper<OverseasInTransitDetailEntity> {

    Page<OverseasInTransitDetailVO> overseasInTransitDetail(@Param("page") Page<OverseasInTransitDetailVO> page,@Param("params") ReplenishmentSuggestionDTO.DetailParamDTO params);
}
