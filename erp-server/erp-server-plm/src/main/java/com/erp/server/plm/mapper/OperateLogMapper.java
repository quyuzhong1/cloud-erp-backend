package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.OperateLogSelectDTO;
import com.erp.model.plm.dto.OperateLogShowDTO;
import com.erp.model.plm.entity.OperateLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 18:19
 */
@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLogEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/5 20:43
     * @param query
     * @param params
     * @param yes
     * @return IPage
     */
    IPage<OperateLogShowDTO> paging(Page query, @Param("params") OperateLogSelectDTO params, Integer yes);

    /**
     * @description:列表查询
     * @author Will
     * @date: 2023/1/6 17:08
     * @param params
     * @return List<OperateLogShowDTO>
     */
    List<OperateLogShowDTO> listSysLog(@Param("params") OperateLogSelectDTO params);

    /**
     * 操作日志-产品变更历史分页查询
     * @author zdy
     * @date: 2025/9/16 16:57
     * @param query
     * @param params
     * @return
     */
    IPage<OperateLogShowDTO.HistoryDTO> getProductChangeHistory(@Param("query") Page<OperateLogShowDTO.HistoryDTO> query, @Param("params") OperateLogShowDTO.PagingParamDTO params);
}
