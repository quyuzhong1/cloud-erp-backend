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
     * @param query  Page
     * @param params LogisticsOrderDTO.PagingParamDTO
     * @return LogisticsOrderDTO.ListDTO
     */
    IPage<LogisticsOrderDTO.ListDTO> paging(Page query, @Param("params") LogisticsOrderDTO.PagingParamDTO params);

    /**
     * 根据id列表查询物流下单信息
     *
     * @param ids List<String>
     * @return List<LogisticsOrderDTO.LogisticsLabelPreviewListDTO>
     */
    List<LogisticsOrderDTO.LogisticsLabelPreviewListDTO> printLogisticsLabelPreview(@Param("ids") List<String> ids);

    /**
     * 获取状态统计
     *
     * @param searchParam LogisticsOrderDTO.PagingParamDTO
     * @return List<LogisticsOrderDTO.TabListDTO>
     */
    List<LogisticsOrderDTO.TabListDTO> tabList(@Param("params") LogisticsOrderDTO.PagingParamDTO searchParam);
}
