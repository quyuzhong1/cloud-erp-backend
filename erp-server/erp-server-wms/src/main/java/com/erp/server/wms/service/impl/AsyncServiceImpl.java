package com.erp.server.wms.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.handler.PlatformSaveHandler;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.ShipmentMarkTypeEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.service.AsyncService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.SoOutstockService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异步服务类
 *
 * @author Jim
 * @date 2024/5/9 17:23
 */
@Slf4j
@Service
public class AsyncServiceImpl implements AsyncService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    @Lazy
    private AsyncService asyncService;

    @Async("wmsErpExecutor")
    @Override
    public void asyncBatchQueryAndUpdateOrderStatus(List<SoB2cEntity> soB2cEntityList) {
        Map<String, List<PlatformOrderQueryDTO>> orderGroupMap = soB2cEntityList.stream()
                .filter(e -> !e.getIsCancel())
                .map(e-> new PlatformOrderQueryDTO(e.getId(), e.getPlatformCode(), e.getDictPlatform(), e.getShopId()))
                .collect(Collectors.groupingBy(PlatformOrderQueryDTO::getDictPlatform));
        if (orderGroupMap.isEmpty()){
            return;
        }
        orderGroupMap.entrySet().parallelStream().peek(e->{
            String dictPlatform = e.getKey();
            List<PlatformOrderQueryDTO> curOrderList = e.getValue();
            PlatformSaveHandler.batchQueryAndUpdateOrderStatus(dictPlatform, curOrderList);
        }).collect(Collectors.toList());
    }

    @Override
    @Async("wmsErpExecutor")
    public void updateLogisticWeight(LogisticsBillDTO.UpdateWeight updateWeight) {

        logisticsBillFeign.updateLogisticWeight(updateWeight);
    }


    @Async("wmsErpExecutor")
    @Override
    public void asyncShipOrder(String soId, String soCode, String dictPlatform, String submitPlatformUniqueKey, String sourceDTOJson, String businessDesc, boolean falseDeliveryFlag) {
        try {
            // 根据提交平台唯一key幂等提交
            submitShipOrder(soId, dictPlatform, falseDeliveryFlag, submitPlatformUniqueKey);
        } catch (Exception e) {
            log.error("【{}】销售单【{}】 标记发货失败 >>>错误信息{}", businessDesc, soCode, ExceptionUtil.stacktraceToString(e));
            // 独立异常
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    soId,
                    SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode(),
                    sourceDTOJson,
                    e.getMessage(),
                    ExceptionUtil.stacktraceToString(e),
                    ""
            );
            soB2cFeign.addSoB2cError(addError);
            log.warn("【{}】销售单【{}】标记发货失败记录结束", businessDesc, soCode);
            return;
        }
        // 成功后删除历史(独立事务)
        SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
        deleteDTO.setType(SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode());
        deleteDTO.setMainId(soId);
        soB2cFeign.deleteError(deleteDTO);
    }


    @Override
    @DataIdempotent(keyIdName = "submitPlatformUniqueKey")
    public List<String> submitShipOrder(String soId, String dictPlatform, boolean falseDeliveryFlag, String submitPlatformUniqueKey) {
        log.info("【{}】销售单【{}】 标记发货开始 >>>提交平台唯一key:{}", dictPlatform, soId, submitPlatformUniqueKey);
        // 查询本单明细有已发货标记跳过触发
        List<SoB2cDetailEntity> detailEntityList =FeignQuery.create(SoB2cDetailEntity.class)
                .eq(SoB2cDetailEntity::getMainId, soId)
                .eq(SoB2cDetailEntity::getIsSignShipped, true)
                .list();
        // 已有成功标记发货明细记录跳过
        if (CollectionUtils.isNotEmpty(detailEntityList)){
            log.warn("【{}】销售单【{}】 本单已标记发货忽略 >>>提交平台唯一key:{}", dictPlatform, soId, submitPlatformUniqueKey);
            return detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        }
        PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
        platformShipOrderDTO.setSoB2cId(soId);
        platformShipOrderDTO.setDictPlatform(dictPlatform);
        platformShipOrderDTO.setSubmitPlatformUniqueKey(submitPlatformUniqueKey);
        platformShipOrderDTO.setFalseDeliveryFlag(falseDeliveryFlag);
        List<String> detailIds = PlatformSaveHandler.shipOrder(platformShipOrderDTO);
        //更新销售明细标识
        soB2cFeign.updateSignShippedByDetailId(detailIds);
        return detailIds;
    }

    /**
     * 自动出库
     * @author will
     * @date 2024/6/28 16:37
     * @param soB2cEntity
     * @param entity
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void soB2cDeliveryAutoOut (SoB2cEntity soB2cEntity, SoB2cDeliveryEntity entity) {

        if (SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(entity.getStatus())){
            throw new ServiceException(ApiError.ERROR_99114);
        }

        TransferDeclareDetailEntity declareDetailEntity = transferDeclareFeign.getBySoId(soB2cEntity.getId());
        if (ObjectUtil.isEmpty(declareDetailEntity)) {
            declareDetailEntity = new TransferDeclareDetailEntity();
        }
        //如果是待上传或上传失败则直接返回
        if (StrUtil.equals(soB2cEntity.getTransferStatus(), TransferStatusEnum.WAIT.getCode()) || StrUtil.equals(declareDetailEntity.getOrderUploadStatus(), TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode()) ||
                StrUtil.equals(declareDetailEntity.getOrderUploadStatus(),TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode())) {
            return;
        }
        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();

        //修改订单状态待发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(entity.getSourceId()));
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(LocalDateTime.now());
        updateDeliveryTimeDTO.setSoDeliveryDTOList(Arrays.asList(new SoB2cDTO.SoDeliveryDTO(entity.getSourceId(),entity.getCode())));
        soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

        String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", UserContext.getDefaultLoginUser().getUserName(), "流水线称重", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "流水线称重");

        if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
            // 调用第三方平台SDK标记发货(独立事务)
            String businessDesc = "包装验货";
            this.asyncShipOrder(soB2cEntity.getId(),
                    soB2cEntity.getCode(),
                    soB2cEntity.getDictPlatform(),
                    soB2cEntity.convertSubmitPlatformUniqueKey(),
                    JSONUtil.toJsonStr(entity),
                    businessDesc, false);
        } else {
            log.warn("【{}】未达到条件:忽略标记平台发货", soB2cEntity.getCode());
        }
        //将发货状态更新为已发货
        entity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
        entity.setDeliveryTime(deliveryTime);
        entity.setShipmentMark(ShipmentMarkTypeEnum.AUTO.getCode());
        soB2cDeliveryService.updateById(entity);

    }

    @Override
    @Async("wmsErpExecutor")
    public void asyncGenerateB2cSoOutstock (String b2cSoId) {
        soOutstockService.generateB2cSoOutstock(b2cSoId);
    }

    /**
     * 异步
     */
    @Override
    @Async("wmsErpExecutor")
    public void syncSoB2cDeliveryAutoOut(SoB2cEntity soB2cEntity, SoB2cDeliveryEntity entity) {
        if(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())){
            return;
        }
        asyncService.soB2cDeliveryAutoOut(soB2cEntity,entity);
        //扣减冻结库存
        Boolean isOut = soB2cDeliveryService.generateOutFreezeError(entity);
        if (isOut) {
            //生成直接调拨单
            Boolean isPush = soB2cDeliveryService.pushTransferInfoError(entity);
            if (isPush) {
                //出库
                soB2cDeliveryService.generateB2cSoOutstock(entity);
            }
        }
    }
    /**
     * 异步
     */
    @Override
    @Async("wmsErpExecutor")
    public void syncAutoOut(SoB2cDeliveryEntity entity) {
        //扣减冻结库存
        Boolean isOut = soB2cDeliveryService.generateOutFreezeError(entity);
        if (isOut) {
            //生成直接调拨单
            Boolean isPush = soB2cDeliveryService.pushTransferInfoError(entity);
            if (isPush) {
                //出库
                soB2cDeliveryService.generateB2cSoOutstock(entity);
            }
        }
    }

    /**
     * 取消三方仓出库单
     * - 如果取消成功
     *   - ERP订单状态自动变更为审核通过-配货中
     *   - 记录日志类型：三方仓出库异常；操作内容：三方仓出库异常，三方仓出库单已自动取消
     * - 如果取消失败
     *   - ERP订单状态不做变更
     *   - 记录日志类型：三方仓出库异常；操作内容：三方仓出库异常，三方仓出库单自动取消失败
     */
    @Override
    @Async("wmsErpExecutor")
    public void asyncCancelThirdWarehouseOrder(SoB2cEntity mainEntity) {
        //调用发货拦截接口
        OperateLogDTO.AddModuleOperateLogDTO operateLogDTO = new OperateLogDTO.AddModuleOperateLogDTO();
        operateLogDTO.setOperation("三方仓出库异常");
        operateLogDTO.setModuleType(ModuleTypeEnum.SO_B2C.getCode());
        operateLogDTO.setBusinessId(mainEntity.getId());
        try {
            BatchResultDTO batchResultDTO = soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(mainEntity.getId(), "三方仓出库异常，自动取消"));
            if(batchResultDTO.getSuccess()){
                //拦截成功，接口会更新订单为待提交-待配货，需要自动变更为审核通过-配货中
                mainEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
                mainEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
                soB2cFeign.updateStatus(mainEntity);
                operateLogDTO.setContent("三方仓出库异常，三方仓出库单已自动取消");
            }else{
                operateLogDTO.setContent("三方仓出库异常，三方仓出库单自动取消失败");
            }
        }catch (Exception e){
            log.error("三方仓出库异常，自动取消异常",e);
        }
        soB2cFeign.addModuleOperateLog(operateLogDTO);
    }

}
