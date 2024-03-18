package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsCfgCostDTO;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 费用管理配置表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Mapper
public interface TmsCfgCostMapper extends BaseMapper<TmsCfgCostEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/18 11:50
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<TmsCfgCostDTO.ListDTO> paging(Page query,@Param("params") TmsCfgCostDTO.PagingParamDTO params);
}
