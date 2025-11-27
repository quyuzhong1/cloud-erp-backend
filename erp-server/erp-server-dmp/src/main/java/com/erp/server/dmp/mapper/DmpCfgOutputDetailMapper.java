package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 推送数据配置明细 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Mapper
public interface DmpCfgOutputDetailMapper extends BaseMapper<DmpCfgOutputDetailEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpCfgOutputDetailDTO.ListDTO> paging(Page query, @Param("params") DmpCfgOutputDetailDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpCfgOutputDetailDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpCfgOutputDetailDTO.ListDTO> listExport(@Param("params") DmpCfgOutputDetailDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpCfgOutputDetailDTO.TabListDTO> tabList(@Param("params") DmpCfgOutputDetailDTO.PagingParamDTO searchParam);
}
