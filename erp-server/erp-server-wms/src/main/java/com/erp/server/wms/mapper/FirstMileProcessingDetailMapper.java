package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.FirstMileProcessingDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 头程虚拟仓订单跟踪明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-02-25
 */
@Mapper
public interface FirstMileProcessingDetailMapper extends BaseMapper<FirstMileProcessingDetailEntity> {
    /**
     * 删除未关联主表的明细数据
     * @author will
     * @date 2025/2/26 09:13
     */
    void deleteUnrelatedDetail();
}
