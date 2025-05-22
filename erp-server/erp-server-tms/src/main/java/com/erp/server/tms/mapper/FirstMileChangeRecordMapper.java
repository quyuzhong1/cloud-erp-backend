package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 头程调整记录 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
 */
@Mapper
public interface FirstMileChangeRecordMapper extends BaseMapper<FirstMileChangeRecordEntity> {
    /**
     * 分页查询
     *
     * @param query
     * @param params
     * @return
     */
    IPage<FirstMileChangeRecordDTO.PagingVO> paging(Page<FirstMileChangeRecordDTO.PagingVO> query, @Param("params") FirstMileChangeRecordDTO.PagingParamDTO params);
}
