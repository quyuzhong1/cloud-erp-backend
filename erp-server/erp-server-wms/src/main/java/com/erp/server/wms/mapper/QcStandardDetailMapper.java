package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.QcStandardDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 质检项目明细表 Mapper 接口
 *
 * @author ERPAgent
 * @since 2026-03-22
 */
@Mapper
public interface QcStandardDetailMapper extends BaseMapper<QcStandardDetailEntity> {
}
