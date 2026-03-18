package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpCfgMqDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 输入输出mq信息 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2026-03-17
 */
@Mapper
public interface DmpCfgMqMapper extends BaseMapper<DmpCfgMqEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpCfgMqDTO.ListDTO> paging(Page query, @Param("params") DmpCfgMqDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpCfgMqDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpCfgMqDTO.ListDTO> listExport(@Param("params") DmpCfgMqDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpCfgMqDTO.TabListDTO> tabList(@Param("params") DmpCfgMqDTO.PagingParamDTO searchParam);
}
