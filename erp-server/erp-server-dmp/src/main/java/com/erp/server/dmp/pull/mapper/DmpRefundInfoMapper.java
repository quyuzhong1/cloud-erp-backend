package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpRefundInfo
 */
@Mapper
public interface DmpRefundInfoMapper extends BaseMapper<DmpRefundInfoEntity> {
    /**
     * 清洗退款订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    List<DmpRefundInfoEntity> cleanRefundList(@Param("pageSize") Integer pageSize, @Param("pageIndex") Integer pageIndex);
}




