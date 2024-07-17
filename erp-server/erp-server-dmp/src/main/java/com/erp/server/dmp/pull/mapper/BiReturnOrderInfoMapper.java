package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpReturnOrderInfo
 */
@Mapper
public interface BiReturnOrderInfoMapper extends BaseMapper<BiReturnOrderInfoEntity> {

    /**
     * 清洗退货订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    List<BiReturnOrderInfoEntity> cleanReturnOrderList(@Param("pageSize") Integer pageSize, @Param("pageIndex") Integer pageIndex);
}




