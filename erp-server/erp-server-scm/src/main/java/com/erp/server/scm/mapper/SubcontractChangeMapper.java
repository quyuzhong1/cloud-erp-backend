package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.model.scm.entity.SubcontractChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 委外变更单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Mapper
public interface SubcontractChangeMapper extends BaseMapper<SubcontractChangeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SubcontractChangeDTO.ListDTO> paging(Page query, @Param("params") SubcontractChangeDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    Integer listCount(@Param("params") SubcontractChangeDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SubcontractChangeDTO.ListDTO> listExport(@Param("params") SubcontractChangeDTO.PagingParamDTO params);
    Page<SubcontractChangeDTO.ListDTO> listExport(@Param("page") Page<SubcontractChangeDTO.ListDTO> page, @Param("params") SubcontractChangeDTO.PagingParamDTO params);

}
