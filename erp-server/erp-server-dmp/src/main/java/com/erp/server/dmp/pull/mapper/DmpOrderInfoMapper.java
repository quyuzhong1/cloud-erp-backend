package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpOrderInfo
 */
@Mapper
public interface DmpOrderInfoMapper extends BaseMapper<DmpOrderInfoEntity> {
    /**
     * 清洗订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    List<DmpOrderInfoEntity> cleanOrderList(@Param("pageSize") Integer pageSize, @Param("pageIndex") Integer pageIndex);
}




