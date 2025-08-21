package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SampleBackInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SampleBackInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 样品退回单 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Mapper
public interface SampleBackInfoMapper extends BaseMapper<SampleBackInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SampleBackInfoDTO.ListDTO> paging(Page query, @Param("params") SampleBackInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SampleBackInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SampleBackInfoDTO.ListDTO> listExport(@Param("params") SampleBackInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SampleBackInfoDTO.TabListDTO> tabList(@Param("params") SampleBackInfoDTO.PagingParamDTO searchParam);
}
