package com.erp.server.srm.mapper;
import com.erp.model.srm.entity.PayableInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.srm.dto.PayableInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-09-25
 */
@Mapper
public interface PayableInfoMapper extends BaseMapper<PayableInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<PayableInfoDTO.ListDTO> paging(Page query, @Param("params") PayableInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") PayableInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<PayableInfoDTO.ListDTO> listExport(@Param("params") PayableInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<PayableInfoDTO.TabListDTO> tabList(@Param("params") PayableInfoDTO.PagingParamDTO searchParam);
}
