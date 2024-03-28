package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsReconciliationCostDTO;
import com.erp.model.tms.entity.TmsReconciliationCostEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.mapper.TmsReconciliationCostMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsReconciliationCostService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 对账费用单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-26
 */
@Slf4j
@Service
public class TmsReconciliationCostServiceImpl extends SuperServiceImpl<TmsReconciliationCostMapper, TmsReconciliationCostEntity> implements TmsReconciliationCostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DmpTaskFeign dmpTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsReconciliationCostDTO.AddDTO addDTO) {
        TmsReconciliationCostEntity tmsReconciliationCostEntity = new TmsReconciliationCostEntity();
        BeanMapperUtils.copy(addDTO, tmsReconciliationCostEntity);

        // 数据处理
        handleData(tmsReconciliationCostEntity);

        log.info("开始新增对账费用单");
        boolean save = super.save(tmsReconciliationCostEntity);
        if(!save) {
            throw new ServiceException("对账费用单保存失败");
        }

        return new BaseResultDTO.AddDTO(tmsReconciliationCostEntity.getId(), tmsReconciliationCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<TmsReconciliationCostDTO.UpdateDTO> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            return Boolean.TRUE;
        }
        List<TmsReconciliationCostEntity> list =  BeanMapperUtils.copyList(TmsReconciliationCostEntity.class, updateList);

        handleUpdateData (list);

        log.info("编辑 开始修改对账费用单数据");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("对账费用单保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsReconciliationCostEntity tmsReconciliationCostEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 更新数据
     */
    private void handleUpdateData(List<TmsReconciliationCostEntity> list) {
       if(CollectionUtils.isEmpty(list)) {
           return;
       }
        List<String> currencyList = list.stream().map(TmsReconciliationCostEntity::getCurrency).distinct().collect(Collectors.toList());

        Map<String, BigDecimal> currencyMap = currencyList.stream().collect(Collectors.toMap(obj -> obj, obj -> dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), obj)));

        for (TmsReconciliationCostEntity tmsReconciliationCostEntity : list) {
           //查询汇率
            BigDecimal rate = currencyMap.get(tmsReconciliationCostEntity.getCurrency());
            if(ObjectUtil.isEmpty(rate)){
               throw new ServiceException("汇率为空，请维护汇率后再提交");
           }
           tmsReconciliationCostEntity.setExchangeRate(rate);
       }
    }
}
