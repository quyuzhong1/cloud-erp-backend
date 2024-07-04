package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpRefundInfo
 */
@Mapper
public interface BiRefundInfoMapper extends BaseMapper<BiRefundInfoEntity> {
    /**
     * 清洗退款订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    List<BiRefundInfoEntity> cleanRefundList(@Param("pageSize") Integer pageSize, @Param("pageIndex") Integer pageIndex);
}




