package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 物流下单表 Mapper 接口
 * </p>
 *
 * @author lei.nie
 * @since 2026-04-20
 */
@Mapper
public interface LogisticsOrderMapper extends BaseMapper<LogisticsOrderEntity> {

    /**
     * 分页查询
     *
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsOrderDTO.ListDTO> paging(Page query, @Param("params") LogisticsOrderDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     *
     * @param params
     * @return
     */
    List<LogisticsOrderDTO.ListDTO> listExport(@Param("params") LogisticsOrderDTO.ExportDTO params);


    /**
     * 获取状态统计
     *
     * @param searchParam
     * @return
     */
    List<LogisticsOrderDTO.TabListDTO> tabList(@Param("params") LogisticsOrderDTO.PagingParamDTO searchParam);
}
