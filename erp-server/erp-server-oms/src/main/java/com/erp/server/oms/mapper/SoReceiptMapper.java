package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.oms.dto.SoReceiptDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 收款单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Mapper
public interface SoReceiptMapper extends BaseMapper<SoReceiptEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SoReceiptDTO.ListDTO> paging(Page query, @Param("params") SoReceiptDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SoReceiptDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SoReceiptDTO.ListDTO> listExport(@Param("params") SoReceiptDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SoReceiptDTO.TabListDTO> tabList(@Param("params") SoReceiptDTO.PagingParamDTO searchParam);

    List<SoReceiptDTO.SoInfoAndReceiptDTO> listSoReceiptBySoCode(@Param("params") SoReceiptDTO.SoSearchDTO dto);
}
