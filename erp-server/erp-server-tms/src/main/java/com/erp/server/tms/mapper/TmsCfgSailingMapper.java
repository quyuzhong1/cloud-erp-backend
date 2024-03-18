package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsCfgSailingDTO;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 截单开船配置 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Mapper
public interface TmsCfgSailingMapper extends BaseMapper<TmsCfgSailingEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/3/18 9:19
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<TmsCfgSailingDTO.ListDTO> paging(Page query,@Param("params") TmsCfgSailingDTO.PagingParamDTO params);
}
