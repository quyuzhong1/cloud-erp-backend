package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 直接调拨单明细表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface TransferInfoDetailMapper extends BaseMapper<TransferInfoDetailEntity> {
    /**
     * @description: 根据来源明细ids查询有效数据
     * @author Will
     * @date: 2023/5/18 10:17
     * @param sourceDetailIds
     * @return List<TransferInfoDetailEntity>
     */
    List<TransferInfoDetailEntity> listSourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
