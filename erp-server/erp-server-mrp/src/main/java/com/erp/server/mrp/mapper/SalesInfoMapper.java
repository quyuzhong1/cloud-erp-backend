package com.erp.server.mrp.mapper;

import com.erp.model.mrp.entity.SalesInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 历史销量信息 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface SalesInfoMapper extends BaseMapper<SalesInfoEntity> {
    /**
     * 查询历史销量
     * @author will
     * @date 2024/9/8 14:22
     * @param detailIdList
     * @return List<SalesInfoEntity>
     */
    List<SalesInfoEntity> listHistorySalesInfo(@Param("detailIdList") List<String> detailIdList);
}
