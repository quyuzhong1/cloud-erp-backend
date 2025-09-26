package com.erp.server.plm.mapper;
import com.erp.model.plm.entity.SkuStdCostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.plm.dto.SkuStdCostDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * sku标准成本表 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Mapper
public interface SkuStdCostMapper extends BaseMapper<SkuStdCostEntity> {

}
