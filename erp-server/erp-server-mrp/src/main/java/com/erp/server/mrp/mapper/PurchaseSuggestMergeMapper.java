package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 建议采购(合并后) Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
@Mapper
public interface PurchaseSuggestMergeMapper extends BaseMapper<PurchaseSuggestMergeEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/10/22 16:01
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseSuggestMergeDTO.ListDTO> paging(Page query,@Param("params") PurchaseSuggestMergeDTO.PagingParamDTO params);
}
