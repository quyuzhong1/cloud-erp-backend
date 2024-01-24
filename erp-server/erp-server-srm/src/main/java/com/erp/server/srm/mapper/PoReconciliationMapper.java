package com.erp.server.srm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 采购对账单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Mapper
public interface PoReconciliationMapper extends BaseMapper<PoReconciliationEntity> {
    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 12:13
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PoReconciliationDTO.ListDTO> paging(Page query,@Param("params") PoReconciliationDTO.PagingParamDTO params);
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:22
     * @param params
     * @return List<ListDTO>
     */
    List<PoReconciliationDTO.ListDTO> listExport(@Param("params") PoReconciliationDTO.PagingParamDTO params);
    /**
     * @description: 获取列表统计
     * @author Will
     * @date: 2024/1/23 16:09
     * @param searchParam
     * @return Integer
     */
    Integer tabList(@Param("params") PoReconciliationDTO.PagingParamDTO searchParam);
}
