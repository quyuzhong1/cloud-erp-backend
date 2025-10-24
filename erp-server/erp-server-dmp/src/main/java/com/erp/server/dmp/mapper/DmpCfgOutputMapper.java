package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 推送数据配置 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpCfgOutputMapper extends BaseMapper<DmpCfgOutputEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpCfgOutputDTO.ListDTO> paging(Page query, @Param("params") DmpCfgOutputDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpCfgOutputDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpCfgOutputDTO.ListDTO> listExport(@Param("params") DmpCfgOutputDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpCfgOutputDTO.TabListDTO> tabList(@Param("params") DmpCfgOutputDTO.PagingParamDTO searchParam);
}
