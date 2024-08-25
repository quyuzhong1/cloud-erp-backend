package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.entity.FirstMileEstimatedBillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * @date 2024-08-16
 * @author tanmujin
 */
@Mapper
public interface FirstMileEstimatedBillMapper extends BaseMapper<FirstMileEstimatedBillEntity> {
    IPage<FirstMileEstimatedBillDTO.View> paging(Page<?> query, @Param("params") FirstMileEstimatedBillDTO.PagingParam params);

    List<FirstMileEstimatedBillDTO.View> listByParamIds(@Param("ids") List<String> ids);

    List<FirstMileEstimatedBillDTO.View> listByParam(@Param("params") FirstMileEstimatedBillDTO.ExportParam params);

    List<FirstMileEstimatedBillDTO.View> listByLogisticsBillIds(@Param("ids") List<String> ids, @Param("status") String status);
}
