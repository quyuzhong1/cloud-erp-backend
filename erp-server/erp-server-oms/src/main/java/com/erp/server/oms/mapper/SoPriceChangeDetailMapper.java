package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 产品销售变更价 明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Mapper
public interface SoPriceChangeDetailMapper extends BaseMapper<SoPriceChangeDetailEntity> {

    /**
     * 根据销售价目详情id查询变更详情
     * @Author Luo_WG
     * @Date 2024/1/9 15:04
     * @param soPriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.SoPriceChangeDetailEntity>
     **/
    List<SoPriceChangeDetailEntity> listBySoPriceDetailIds(@Param("soPriceDetailIds") List<String> soPriceDetailIds);
}
