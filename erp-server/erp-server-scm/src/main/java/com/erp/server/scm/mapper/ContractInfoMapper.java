package com.erp.server.scm.mapper;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.scm.dto.ContractInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 合同管理表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-06-16
 */
@Mapper
public interface ContractInfoMapper extends BaseMapper<ContractInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<ContractInfoDTO.ListDTO> paging(Page query, @Param("params") ContractInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") ContractInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<ContractInfoDTO.ListDTO> listExport(@Param("params") ContractInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<ContractInfoDTO.TabListDTO> tabList(@Param("params") ContractInfoDTO.PagingParamDTO searchParam);
}
