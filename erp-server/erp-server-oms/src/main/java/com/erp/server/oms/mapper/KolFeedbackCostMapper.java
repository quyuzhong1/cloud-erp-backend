package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.KolFeedbackCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.KolFeedbackCostDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * KOL回片费用表 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Mapper
public interface KolFeedbackCostMapper extends BaseMapper<KolFeedbackCostEntity> {

    /**
     * 分页查询
     * @param page
     * @param params
     * @return
     */
    IPage<KolFeedbackCostDTO.ListDTO> paging(Page<KolFeedbackCostDTO.ListDTO> page, @Param("params") KolFeedbackCostDTO.ParamDTO params);

}
