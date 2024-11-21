package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 要货申请变更单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
@Mapper
public interface RequisitionApplicationChangeMapper extends BaseMapper<RequisitionApplicationChangeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<RequisitionApplicationChangeDTO.ListDTO> paging(Page query, @Param("params") RequisitionApplicationChangeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") RequisitionApplicationChangeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<RequisitionApplicationChangeDTO.ListDTO> listExport(@Param("params") RequisitionApplicationChangeDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<RequisitionApplicationChangeDTO.TabListDTO> tabList(@Param("params") RequisitionApplicationChangeDTO.PagingParamDTO searchParam);

    List<RequisitionApplicationChangeDetailEntity> listNotHandleDetailByBusinessDetailIds(@Param("businessDetailIds") List<String> businessDetailIds);
}
