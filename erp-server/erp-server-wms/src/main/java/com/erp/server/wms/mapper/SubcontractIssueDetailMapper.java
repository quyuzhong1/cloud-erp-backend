package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 委外发料明细单 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Mapper
public interface SubcontractIssueDetailMapper extends BaseMapper<SubcontractIssueDetailEntity> {
    /**
     * @description: 根据来源明细id集合
     * @author Will
     * @date: 2024/1/9 16:58
     * @param sourceDetailIdList
     * @return List<SubcontractIssueDetailEntity>
     */
    List<SubcontractIssueDetailEntity> listBySourceDetailIdList(@Param("sourceDetailIdList") List<String> sourceDetailIdList);
    /**
     * @description: 根据委外订单明细id集合查询
     * @author Will
     * @date: 2024/2/23 11:48
     * @param subcontractOrderDetailIdList
     * @return List<SubcontractIssueDetailEntity>
     */
    List<SubcontractIssueDetailEntity> listBySubcontractOrderDetailIdList(@Param("subcontractOrderDetailIdList") List<String> subcontractOrderDetailIdList);
}
