package com.erp.server.wms.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.InventoryTransactionDTO.CheckInventoryDTO;
import com.erp.model.wms.entity.InventoryTransactionEntity;


/**
 * <p>
 * 库存事务表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
 */
@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransactionEntity> {
	List<CheckInventoryDTO> queryDbInventoryCheckSame(@Param("ids")List<String> ids);
}
