package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.bi.dto.DmpShopChangeLogDTO;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import com.erp.server.bi.mapper.DmpShopChangeLogMapper;
import com.erp.server.bi.service.DmpShopChangeLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 14:30
 */
@Service
public class DmpShopChangeLogServiceImpl extends ServiceImpl<DmpShopChangeLogMapper, DmpShopChangeLogEntity>
        implements DmpShopChangeLogService {


    @Override
    public List<DmpShopChangeLogDTO> listByShopId(String shopId) {
        LambdaQueryWrapper<DmpShopChangeLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpShopChangeLogEntity::getShopId,shopId);
        List<DmpShopChangeLogEntity> list = this.list(queryWrapper);
        List<DmpShopChangeLogDTO> dmpShopChangeLogList = BeanMapperUtils.copyList(DmpShopChangeLogDTO.class, list);
        return dmpShopChangeLogList;
    }
}
