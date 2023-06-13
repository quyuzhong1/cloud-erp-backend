package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售需求明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface SalesDemandDetailMapper extends BaseMapper<SalesDemandDetailEntity> {
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/5/23 9:58
     * @param sourceDetailIds
     * @return List<SalesDemandDetailEntity>
     */
    List<SalesDemandDetailEntity> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
