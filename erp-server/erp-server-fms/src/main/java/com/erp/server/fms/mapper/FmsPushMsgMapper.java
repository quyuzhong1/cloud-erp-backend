package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.FmsPushMsgEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.FmsPushMsgDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 本地推送消息表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-12-30
 */
@Mapper
public interface FmsPushMsgMapper extends BaseMapper<FmsPushMsgEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<FmsPushMsgDTO.ListDTO> paging(Page query, @Param("params") FmsPushMsgDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") FmsPushMsgDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<FmsPushMsgDTO.ListDTO> listExport(@Param("params") FmsPushMsgDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<FmsPushMsgDTO.TabListDTO> tabList(@Param("params") FmsPushMsgDTO.PagingParamDTO searchParam);
}
