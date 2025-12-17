package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.CustomerCreditApplyEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 客户授信 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Mapper
public interface CustomerCreditApplyMapper extends BaseMapper<CustomerCreditApplyEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CustomerCreditApplyDTO.ListDTO> paging(Page query, @Param("params") CustomerCreditApplyDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CustomerCreditApplyDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CustomerCreditApplyDTO.ListDTO> listExport(@Param("params") CustomerCreditApplyDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CustomerCreditApplyDTO.TabListDTO> tabList(@Param("params") CustomerCreditApplyDTO.PagingParamDTO searchParam);
}
