package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 委外订单明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Mapper
public interface SubcontractOrderDetailMapper extends BaseMapper<SubcontractOrderDetailEntity> {

    /**
     * @description: 根据来源明细ids查询有效数据
     * @author Will
     * @date: 2023/6/14 15:08
     * @param sourceDetailIds
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
