package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 委外订单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Mapper
public interface SubcontractOrderMapper extends BaseMapper<SubcontractOrderEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SubcontractOrderDTO.ListDTO> paging(Page query, @Param("params") SubcontractOrderDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    Integer listCount(@Param("params") SubcontractOrderDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SubcontractOrderDTO.ListDTO> listExport(@Param("params") SubcontractOrderDTO.ExportDTO params);

}
