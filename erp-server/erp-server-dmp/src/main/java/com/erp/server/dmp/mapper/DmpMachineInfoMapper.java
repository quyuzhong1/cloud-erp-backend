package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpMachineInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpMachineInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 加工单 Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
@Mapper
public interface DmpMachineInfoMapper extends BaseMapper<DmpMachineInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpMachineInfoDTO.ListDTO> paging(Page query, @Param("params") DmpMachineInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpMachineInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpMachineInfoDTO.ListDTO> listExport(@Param("params") DmpMachineInfoDTO.ExportDTO params);

}
