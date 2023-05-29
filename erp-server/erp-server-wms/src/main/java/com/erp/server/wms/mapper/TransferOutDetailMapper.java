package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 分布式调出单明细 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface TransferOutDetailMapper extends BaseMapper<TransferOutDetailEntity> {

    /**
     * 根据来源明细id查询有效数据
     * @param sourceDetailIds
     * @return
     */
    List<TransferOutDetailEntity> listSourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
