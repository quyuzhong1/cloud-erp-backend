package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapEntity;
import com.erp.model.dmp.vo.CfgApiFieldMapVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:40
 */
@Mapper
public interface CfgApiFieldMapMapper extends BaseMapper<CfgApiFieldMapEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/1/11 12:26
     * @param query
     * @param params
     * @return IPage<CfgApiFieldMapVO>
     */
    IPage<CfgApiFieldMapVO> paging(Page query, @Param("params") BaseSearchDTO params);
}
