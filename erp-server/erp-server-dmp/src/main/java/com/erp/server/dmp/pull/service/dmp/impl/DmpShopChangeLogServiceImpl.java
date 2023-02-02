package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import com.erp.server.dmp.pull.mapper.DmpShopChangeLogMapper;
import com.erp.server.dmp.pull.service.dmp.DmpShopChangeLogService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class DmpShopChangeLogServiceImpl extends ServiceImpl<DmpShopChangeLogMapper, DmpShopChangeLogEntity>
        implements DmpShopChangeLogService {

    /**
     * 查询店铺负责人
     * @Author Luo_WG
     * @Date 2022/12/29 16:21
     * @param shopId shopId
     * @param date date
     * @return com.erp.model.dmp.entity.DmpShopChangeLogEntity
     **/
    @Override
    public DmpShopChangeLogEntity getShopChargeName(String shopId, LocalDateTime date) {
        return baseMapper.getShopChargeName(shopId,date);
    }
}
