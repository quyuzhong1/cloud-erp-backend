package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.OverseasHistoryInventoryDTO;
import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 海外仓库存 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Mapper
public interface OverseasHistoryInventoryMapper extends BaseMapper<OverseasHistoryInventoryEntity> {

    /**
     * 分页查询
     * @param query 分页
     * @param params 参数
     */
    IPage<OverseasHistoryInventoryDTO.ListDTO> paging(Page<OverseasHistoryInventoryDTO.ListDTO> query, OverseasHistoryInventoryDTO.PagingParamDTO params);
}
