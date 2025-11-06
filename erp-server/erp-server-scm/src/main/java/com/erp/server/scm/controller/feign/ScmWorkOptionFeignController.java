package com.erp.server.scm.controller.feign;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@Slf4j
@RestController
@RequestMapping("feign/scmWorkOption")
public class ScmWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    @Resource
    private SalesDemandService salesDemandService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private PurchaseApplicationService purchaseApplicationService;
    @Resource
    private PurchaseChangeDetailService purchaseChangeDetailService;
    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;
    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private AssetNoticeService assetNoticeService;

    @Resource
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Resource
    private AssetPurchaseChangeService assetPurchaseChangeService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        return workOptionService.getTableNum(myWorkOptionDTOList);
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:35
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/salesDemandApprove")
    public List<BatchResultDTO> salesDemandApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SalesDemandEntity> entityList = salesDemandService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SalesDemandEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"备货申请单不存在"));
                continue;
            }
            try {
                resultDTOS.add(salesDemandService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("备货申请单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:35
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchasePriceApprove")
    public List<BatchResultDTO> purchasePriceApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceEntity> entityList = purchasePriceService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购价目审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchasePriceChangeApprove")
    public List<BatchResultDTO> purchasePriceChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceChangeEntity> entityList = purchasePriceChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目变更记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceChangeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购价目审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchaseOrderApprove")
    public Boolean purchaseOrderApprove(@RequestBody @Validated ApproveOneDTO dto) {
        purchaseOrderService.approve(dto);
        return Boolean.TRUE;
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchaseChangeApprove")
    public List<BatchResultDTO> purchaseChangeApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchaseChangeEntity> entityList = purchaseChangeService.listByIds(dto.getIds());
        //查询原采购订单明细信息
        List<PurchaseChangeDetailEntity> purchaseChangeDetailEntityList = purchaseChangeDetailService.listByPurchaseChangeIds(dto.getIds());
        List<String> purchaseOrderDetailIds = purchaseChangeDetailEntityList.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList =  purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        //修改到货状态
        List<String> podIds = purchaseChangeDetailEntityList.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PoReturnDetailEntity> returnDetailEntityList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        List<WarehouseReceiveDetailEntity> receiveDetailEntityList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        for (String id : dto.getIds()) {
            PurchaseChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购变更单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseChangeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),
                        purchaseChangeDetailEntityList, purchaseOrderDetailEntityList,returnDetailEntityList, receiveDetailEntityList));
            }catch (Exception e){
                log.error("采购变更单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/16 14:48
     * @param dto dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/purchaseApplicationApprove")
    public List<BatchResultDTO> purchaseApplicationApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchaseApplicationEntity> entityList = purchaseApplicationService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchaseApplicationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购申请单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseApplicationService.approve(entity,new ApproveOneDTO(entity.getId(),dto.getType(),dto.getComment(),dto.getIsNeedProcess())));
            }catch (Exception e){
                log.error("采购申请单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    @PostMapping("/assetNoticeApprove")
    public List<BatchResultDTO> assetNoticeApprove(@RequestBody @Validated BaseApproveParamDTO  dto){
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<AssetNoticeEntity> list = assetNoticeService.lambdaQuery().in(AssetNoticeEntity::getId, ids).list();
        Map<String, AssetNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetNoticeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetNoticeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("审核失败",e);
                AssetNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS;
    }

    @PostMapping("/assetPurchaseOrderApprove")
    public List<BatchResultDTO> assetPurchaseOrderApprove(@RequestBody @Validated BaseApproveParamDTO  dto){
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AssetPurchaseOrderEntity> list = assetPurchaseOrderService.lambdaQuery().in(AssetPurchaseOrderEntity::getId, ids).list();
        Map<String, AssetPurchaseOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseOrderEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetPurchaseOrderService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("审核失败",e);
                AssetPurchaseOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS;
    }

    @PostMapping("/assetPurchaseChangeApprove")
    public List<BatchResultDTO> assetPurchaseChangeApprove(@RequestBody @Validated BaseApproveParamDTO  dto){
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AssetPurchaseChangeEntity> list = assetPurchaseChangeService.lambdaQuery().in(AssetPurchaseChangeEntity::getId, ids).list();
        Map<String, AssetPurchaseChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(AssetPurchaseChangeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = assetPurchaseChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("审核失败",e);
                AssetPurchaseChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS;
    }
}
