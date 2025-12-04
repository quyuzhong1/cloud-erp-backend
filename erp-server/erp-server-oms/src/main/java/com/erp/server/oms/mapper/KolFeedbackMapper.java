package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.KolFeedbackDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * KOL回片列表 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Mapper
public interface KolFeedbackMapper extends BaseMapper<KolFeedbackEntity> {

    /**
     * 分页查询
     * @param page
     * @param params
     * @return
     */
    IPage<KolFeedbackDTO.ListDTO> paging(Page<KolFeedbackDTO.ListDTO> page, @Param("params") KolFeedbackDTO.ParamDTO params);

    /**
     * 状态统计
     * @param params
     * @return
     */
    List<KolFeedbackDTO.TabListDTO> tabList(@Param("params") KolFeedbackDTO.ParamDTO params);
    /**
     * 根据来源明细查询回片数量
     * @author will 
     * @date 2025/12/2 16:32
     * @param sourceDetailIdList 
     * @return List<FeedbackQtyDTO>
     */
    List<KolFeedbackDTO.FeedbackQtyDTO> listFeedbackQtyBySourceDetailIdList(@Param("sourceDetailIdList")List<String> sourceDetailIdList);
}
