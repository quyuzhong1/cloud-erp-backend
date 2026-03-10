package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.VirtualInventoryTransactionDTO;
import com.erp.model.wms.dto.InventoryTransactionDTO.CheckInventoryDTO;
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

	List<CheckInventoryDTO> queryDbInventoryCheckSame(@Param("ids")List<String> ids);
}
