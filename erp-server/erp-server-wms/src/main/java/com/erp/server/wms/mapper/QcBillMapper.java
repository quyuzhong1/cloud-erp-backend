package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.QcBillEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 质检单表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Mapper
public interface QcBillMapper extends BaseMapper<QcBillEntity> {

}
