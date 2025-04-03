package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2C销售订单合并拆分关联表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cRefMapper extends BaseMapper<SoB2cRefEntity> {
    /**
     * @description: 根据目标单据ids和操作类型查询来源下说有的关联关系
     * @author Will
     * @date: 2023/12/13 19:43
     * @param targetIdList
     * @param code
     * @return List<SoB2cRefEntity>
     */
    List<SoB2cRefEntity> listSourceByTargetIds(@Param("targetIdList") List<String> targetIdList,@Param("code") String code);

    List<String> getAllSplitIds(@Param("soId") String soId);
}
