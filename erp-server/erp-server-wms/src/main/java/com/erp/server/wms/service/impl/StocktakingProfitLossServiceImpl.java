package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.server.wms.mapper.StocktakingProfitLossMapper;
import com.erp.server.wms.service.StocktakingProfitLossService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘盈盘亏单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingProfitLossServiceImpl extends SuperServiceImpl<StocktakingProfitLossMapper, StocktakingProfitLossEntity> implements StocktakingProfitLossService {

    @Reference
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    /**
     * 盘点任务单审核通过生成盘盈盘亏单
     *
     * @param taskEntity
     * @return void
     * 如果当前存在事务就加入 如果不存在就创建一个新的
     * @author yl
     * @date 2023-08-10 11:52
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void autoCreateBill(StocktakingTaskEntity taskEntity) {
        if (Objects.isNull(taskEntity)) {
            return;
        }
        //任务code
        String taskCode = taskEntity.getCode();
        //任务id
        String taskId = taskEntity.getId();
        List<StocktakingTaskDetailEntity> stocktakingTaskDetailList = stocktakingTaskDetailService.listBaseByMainIds(Arrays.asList(taskId));
        //以仓库分组
        Map<String, List<StocktakingTaskDetailEntity>> warehouseMap = stocktakingTaskDetailList.stream().collect(Collectors.groupingBy(StocktakingTaskDetailEntity::getWarehouseId));
        //盘盈盘亏单
        List<StocktakingProfitLossEntity> addList = new ArrayList<>(stocktakingTaskDetailList.size());
        //盘盈盘亏单 明细
        List<StocktakingProfitLossDetailEntity> addDetailList = new ArrayList<>(10);
        //单据日期
        LocalDate billDate = LocalDate.now();
        //盘亏
        BillTypeEnum loss = BillTypeEnum.LOSS;
        //盘盈
        BillTypeEnum profit = BillTypeEnum.PROFIT;
        for (Map.Entry<String, List<StocktakingTaskDetailEntity>> item : warehouseMap.entrySet()) {
            //仓库id
            String warehouseId = item.getKey();

            List<StocktakingTaskDetailEntity> taskDetailList = item.getValue();
            //盘盈的任务明细
            List<StocktakingTaskDetailEntity> profitDetailList = taskDetailList.stream().filter(d -> d.getDiffQty() > 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(profitDetailList)) {

            }
            //盘亏的任务明细
            List<StocktakingTaskDetailEntity> lossDetailList = taskDetailList.stream().filter(d -> d.getDiffQty() < 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(lossDetailList)) {
                disposeDb(taskId, taskCode, lossDetailList, billDate,profit);
            }


        }

    }

    /**
     * 处理数据
     *
     * @param sourceId
     * @param sourceCode
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-08-10 15:05
     */
    private Map<String, Object> disposeDb(String sourceId, String sourceCode, List<StocktakingTaskDetailEntity> detailList, LocalDate billDate, BillTypeEnum billType) {
        List<StocktakingProfitLossDetailEntity> detailEntityList = new ArrayList<>(detailList.size());
        StocktakingProfitLossEntity profitEntity = new StocktakingProfitLossEntity();
        profitEntity.setBillDate(billDate);
        profitEntity.setBillType(billType);
        profitEntity.setSourceId(sourceId);
        profitEntity.setSourceCode(sourceCode);
        String id = IdWorker.getIdStr();
        profitEntity.setId(id);
        BusinessNoTypeEnum businessNoType = BusinessNoTypeEnum.STOCKTAKING_PROFIT;
        //盘亏单
        if (BillTypeEnum.LOSS.equals(billType)) {
            businessNoType = BusinessNoTypeEnum.STOCKTAKING_LOSS;
        }

        String code = docNoGenHelper.generateCode(businessNoType);
        profitEntity.setCode(code);
        for (StocktakingTaskDetailEntity detail : detailList) {
            //明细
            StocktakingProfitLossDetailEntity profitDetail = new StocktakingProfitLossDetailEntity();
            profitDetail.setMainId(id);
            profitDetail.setDiffQty(detail.getDiffQty());
            profitDetail.setFrozenQty(detail.getFrozenQty());
            profitDetail.setQty(detail.getQty());
            profitDetail.setSkuId(detail.getSkuId());
            profitDetail.setSkuNo(detail.getSkuNo());
            profitDetail.setUsableQty(detail.getUsableQty());
            profitDetail.setWarehouseId(detail.getWarehouseId());
            profitDetail.setWarehouseLocation(detail.getWarehouseLocation());
            profitDetail.setSourceDetailId(detail.getId());
            detailEntityList.add(profitDetail);
        }

        Map<String, Object> map = new HashMap<>(2);
        map.put("stocktakingProfitLoss", profitEntity);
        map.put("detailEntityList", detailEntityList);

        return map;
    }
}
