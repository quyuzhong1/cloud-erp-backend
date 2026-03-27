package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcStandardSkuRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcStandardSkuRefDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 质检标准关联SKU记录表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-26
 */
@Mapper
public interface QcStandardSkuRefMapper extends BaseMapper<QcStandardSkuRefEntity> {

}
