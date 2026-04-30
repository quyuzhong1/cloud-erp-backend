package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 报关明细中间表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-04-27
 */
@Mapper
public interface DeliveryDeclareDetailMidMapper extends BaseMapper<DeliveryDeclareDetailMidEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DeliveryDeclareDetailMidDTO.ListDTO> paging(Page query, @Param("params") DeliveryDeclareDetailMidDTO.PagingParamDTO params);


    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DeliveryDeclareDetailMidDTO.ListDTO> listExport(@Param("params") DeliveryDeclareDetailMidDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(@Param("params") DeliveryDeclareDetailMidDTO.PagingParamDTO searchParam);
}
