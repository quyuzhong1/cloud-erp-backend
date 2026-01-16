package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpAwdShipmentDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpAwdShipmentDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * FBA拣货明细表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-12-23
 */
@Mapper
public interface DmpAwdShipmentDetailMapper extends BaseMapper<DmpAwdShipmentDetailEntity> {
}
