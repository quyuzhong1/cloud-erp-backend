package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


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
    IPage<CfgRuleWaveDTO.ListDTO> paging(Page query,@Param("params") CfgRuleWaveDTO.PagingParamDTO params);
}
