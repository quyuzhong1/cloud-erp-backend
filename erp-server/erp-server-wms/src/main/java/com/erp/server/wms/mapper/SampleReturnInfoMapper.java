package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleReturnInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品归还单主表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Mapper
public interface SampleReturnInfoMapper extends BaseMapper<SampleReturnInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleReturnInfoDTO.ListDTO> paging(Page query, @Param("params") SampleReturnInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleReturnInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SampleReturnInfoDTO.ListDTO> listExport(@Param("params") SampleReturnInfoDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleReturnInfoDTO.TabListDTO> tabList(@Param("params") SampleReturnInfoDTO.PagingParamDTO searchParam);
}
