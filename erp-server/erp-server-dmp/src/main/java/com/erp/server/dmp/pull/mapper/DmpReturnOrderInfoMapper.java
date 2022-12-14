package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpReturnOrderInfo
 */
@Mapper
public interface DmpReturnOrderInfoMapper extends BaseMapper<DmpReturnOrderInfoEntity> {


    /**
     * 清洗退货订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    List<DmpReturnOrderInfoEntity> cleanReturnOrderList(@Param("pageSize") Integer pageSize, @Param("pageIndex") Integer pageIndex);
}




