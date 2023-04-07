package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.server.dmp.entity.DmpWarehouseInboundItemEntity;
import com.erp.server.dmp.entity.DmpWarehouseInboundRecordEntity;
import com.erp.server.dmp.mapper.DmpWarehouseInboundRecordMapper;
import com.erp.server.dmp.service.DmpWarehouseInboundItemService;
import com.erp.server.dmp.service.DmpWarehouseInboundRecordService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓上架记录表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-03-29
 */
@Service
public class DmpWarehouseInboundRecordServiceImpl extends SuperServiceImpl<DmpWarehouseInboundRecordMapper, DmpWarehouseInboundRecordEntity> implements DmpWarehouseInboundRecordService {

    @Resource
    private DmpWarehouseInboundItemService dmpWarehouseInboundItemService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkOrder(GoodcangDTO.MessageDTO ext) {
        // 入库单是否存在
        Optional<DmpWarehouseInboundRecordEntity> entityOptional = lambdaQuery()
                .eq(DmpWarehouseInboundRecordEntity::getReceivingCode, ext.getReceivingCode())
                .oneOpt();
        if(entityOptional.isPresent()){
            return Boolean.FALSE;
        }
        if (CollectionUtil.isEmpty(ext.getReceivingDetail())) {
            throw new RuntimeException("海外仓入库单不存在详情信息");
        }
        // 不存在新增
        DmpWarehouseInboundRecordEntity insertRecord = new DmpWarehouseInboundRecordEntity(ext);
        boolean result  = save(insertRecord);
        // 存在更新
        if(!result){
            throw new RuntimeException("海外仓入库单主数据保存/更新失败");
        }
        List<DmpWarehouseInboundItemEntity> insertList = ext.getReceivingDetail()
                .stream()
                .map(x -> new DmpWarehouseInboundItemEntity(x, insertRecord.getId()))
                .collect(Collectors.toList());
        dmpWarehouseInboundItemService.saveBatch(insertList);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean process(GoodcangDTO.MessageDTO ext) {
        // 先保存数据数据，以支持错误回滚
        checkOrder(ext);
        // 推送金蝶 TODO
        return Boolean.TRUE;
    }
}
