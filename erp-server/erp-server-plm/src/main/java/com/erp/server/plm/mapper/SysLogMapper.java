package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.SysLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:19
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLogEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/5 20:43
     * @param query
     * @param params
     * @param yes
     * @return IPage
     */
    IPage<SysLogShowDTO> paging(Page query,@Param("params") SysLogSelectDTO params, Integer yes);

    /**
     * @description:列表查询
     * @author Will
     * @date: 2023/1/6 17:08
     * @param params
     * @return List<SysLogShowDTO>
     */
    List<SysLogShowDTO> listSysLog(@Param("params") SysLogSelectDTO params);
}
