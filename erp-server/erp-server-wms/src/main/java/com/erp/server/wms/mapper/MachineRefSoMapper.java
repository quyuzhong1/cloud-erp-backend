package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.MachineRefSoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 加工单和销售订单关联表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-12-06
 */
@Mapper
public interface MachineRefSoMapper extends BaseMapper<MachineRefSoEntity> {

}
