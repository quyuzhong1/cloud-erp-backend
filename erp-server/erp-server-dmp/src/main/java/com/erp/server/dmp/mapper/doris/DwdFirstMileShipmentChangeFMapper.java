package com.erp.server.dmp.mapper.doris;
import com.erp.model.dmp.entity.doris.DwdFirstMileShipmentChangeFEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DwdFirstMileShipmentChangeFDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * DWD头程发货签收变更记录(包含期初/调整) Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-11-28
 */
@Mapper
public interface DwdFirstMileShipmentChangeFMapper extends BaseMapper<DwdFirstMileShipmentChangeFEntity> {


}
