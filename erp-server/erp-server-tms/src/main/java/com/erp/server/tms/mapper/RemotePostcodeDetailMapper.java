package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 偏远邮编明细表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
@Mapper
public interface RemotePostcodeDetailMapper extends BaseMapper<RemotePostcodeDetailEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<RemotePostcodeDetailDTO.ListDTO> paging(Page query, @Param("params") RemotePostcodeDetailDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") RemotePostcodeDetailDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<RemotePostcodeDetailDTO.ListDTO> listExport(@Param("params") RemotePostcodeDetailDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<RemotePostcodeDetailDTO.TabListDTO> tabList(@Param("params") RemotePostcodeDetailDTO.PagingParamDTO searchParam);
}
