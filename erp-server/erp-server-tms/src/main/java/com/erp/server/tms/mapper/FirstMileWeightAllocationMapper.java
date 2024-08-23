package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 头程重量分摊 Mapper 接口
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
@Mapper
public interface FirstMileWeightAllocationMapper extends BaseMapper<FirstMileWeightAllocationEntity> {

    int tabCount(@Param("status") String costAllocationStatus);

    IPage<FirstMileWeightAllocationDTO.ViewDTO> paging(Page<?> query, @Param("params") FirstMileWeightAllocationDTO.PagingParamDTO params);

    List<FirstMileWeightAllocationDTO.ViewDTO> listByParamIds(@Param("ids") List<String> ids);

    List<FirstMileWeightAllocationDTO.ViewDTO> listByParam(@Param("params") FirstMileWeightAllocationDTO.ExportParamDTO dto);
    /**
     * 根据来源id查询重量分摊列表
     * @param sourceIds
     * @param statusList
     * @return
     */
    List<FirstMileWeightAllocationEntity> listBySourceIds(@Param("sourceIds") List<String> sourceIds, @Param("statusList") List<String> statusList);
}
