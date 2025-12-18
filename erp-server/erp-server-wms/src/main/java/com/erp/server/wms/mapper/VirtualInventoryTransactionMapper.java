package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.VirtualInventoryTransactionDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 虚拟仓库存事务表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-12-18
 */
@Mapper
public interface VirtualInventoryTransactionMapper extends BaseMapper<VirtualInventoryTransactionEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<VirtualInventoryTransactionDTO.ListDTO> paging(Page query, @Param("params") VirtualInventoryTransactionDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") VirtualInventoryTransactionDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<VirtualInventoryTransactionDTO.ListDTO> listExport(@Param("params") VirtualInventoryTransactionDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<VirtualInventoryTransactionDTO.TabListDTO> tabList(@Param("params") VirtualInventoryTransactionDTO.PagingParamDTO searchParam);
}
