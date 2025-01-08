package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.RemotePostcodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 偏远邮编组 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
@Mapper
public interface RemotePostcodeMapper extends BaseMapper<RemotePostcodeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<RemotePostcodeDTO.ListDTO> paging(Page query, @Param("params") RemotePostcodeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") RemotePostcodeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    IPage<RemotePostcodeDTO.ExportListDTO> listExport(Page query, @Param("params") RemotePostcodeDTO.ExportDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<RemotePostcodeDTO.TabListDTO> tabList(@Param("params") RemotePostcodeDTO.PagingParamDTO searchParam);

    IPage<RemotePostcodeDTO.ListDTO> pagingSelect(Page query, @Param("params")RemotePostcodeDTO.SelectDTO params);

    List<RemotePostcodeDTO.ListDTO> select(@Param("params")RemotePostcodeDTO.SelectDTO params);
}
