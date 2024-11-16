package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 波次规则 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Mapper
public interface CfgRuleWaveMapper extends BaseMapper<CfgRuleWaveEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/6/24 16:33
     * @param query
     * @param params
     * @return IPage
     */
    IPage<CfgRuleWaveDTO.ListDTO> paging(Page<CfgRuleWaveDTO.ListDTO> query,@Param("params") CfgRuleWaveDTO.PagingParamDTO params);

    /**
     * 根据时间查询规则配置
     *
     * @param time
     * @author will
     * @date 2024/6/27 16:43
     */
    List<CfgRuleWaveEntity> listRuleWaveByTime(@Param("time") String time);
}
