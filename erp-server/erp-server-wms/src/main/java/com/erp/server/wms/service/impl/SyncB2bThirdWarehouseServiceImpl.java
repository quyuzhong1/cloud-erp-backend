package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.enums.B2bPackingTypeEnum;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncB2bThirdWarehouseServiceImpl implements SyncB2bThirdWarehouseService {

    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private WmsPushMsgService wmsPushMsgService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private FileFeign fileFeign;


    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private B2bCustomerPackingService b2bCustomerPackingService;

    @Override
    public DmpPushTaskEntity syncB2bThirdWarehouse(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList, String operate) {
        //生成任务
        if (SyncOperateEnum.OPERATE_ADD.getCode().equals(operate)) {
            return saveCreateTask(entity, operate, this.newSyncDataToThirdWarehouseCreate(entity, detailEntityList));
        }else if (SyncOperateEnum.OPERATE_INVALID.getCode().equals(operate)){
            return saveCancelTask(entity, operate, this.newSyncDataToThirdWarehouseCancel(entity));
        } else {
            throw new ServiceException("操作类型【{}】不支持同步B2B第三方发货单", operate);
        }
    }

    private DmpPushTaskEntity saveCancelTask(B2bThirdDeliveryEntity entity, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.B2B_THIRD_DELIVERY_CANCEL.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        String msg = StrUtil.format("用户【{}】操作【{}】单据单号为【{}】异步拦截三方仓出库订单，等待三方仓处理/未同步三方仓", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单" , entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "【中台任务】发货拦截");

        if (CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY_CANCEL.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_B2B_THIRD_DELIVERY_CANCEL_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.ERP_B2B_THIRD_WAREHOUSE_CANCEL_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }

        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY_CANCEL.getCode());
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        wmsPushMsgEntity.setTargetPlatform(PlatformEnum.ERP.getName());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);
        return null;
    }

    @Override
    public Map<String, Object> newSyncDataToThirdWarehouseCreate(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList) {
        OverseasProviderEntity overseasProviderEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDeliveryWarehouseId());
        if (Objects.isNull(overseasProviderEntity)){
            throw new ServiceException(ApiError.WH_OVERSEAS_PROVIDER_NOT_FOUND);
        }
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(entity.getId()), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode());
        ThirdWarehouseCreateFbaOutboundReq req = B2bThirdDeliveryConverter.INSTANCE.toCreateFbaOutboundReq(entity, detailEntityList);
        fillPackingForOutboundReq(req, entity);

        OverseasProviderWarehouseEntity overseasProviderWarehouse = overseasProviderWarehouseService.getByWarehouseId(entity.getDeliveryWarehouseId());
        if (Objects.nonNull(overseasProviderWarehouse)) {
            req.setMappingWarehouseCode(overseasProviderWarehouse.getPlatformWarehouseCode());
        }

        List<ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO> warehouseOperationTypeDTOList = ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO.convert(
                entity.getWarehouseOperationType(),
                entity.getOperationDesc()
        );
        if (!req.getItems().isEmpty()) {
            List<String> soDetailIds = detailEntityList.stream().map(B2bThirdDeliveryDetailEntity::getSoDetailId).collect(Collectors.toList());
            List<SoDetailEntity> soDetails = soInfoFeign.listSoDetailByIds(soDetailIds);

            if (!soDetails.isEmpty()) {
                // 创建 soDetailId 到 platformSkuNo 的映射
                Map<String, String> soDetailSkuMap = soDetails.stream()
                        .collect(Collectors.toMap(SoDetailEntity::getId, SoDetailEntity::getPlatformSkuNo));

                // 创建 detailId 到 soDetailId 的映射
                Map<String, String> detailSoDetailMap = detailEntityList.stream()
                        .collect(Collectors.toMap(B2bThirdDeliveryDetailEntity::getId, B2bThirdDeliveryDetailEntity::getSoDetailId));

                for (ThirdWarehouseCreateFbaOutboundReq.Item item : req.getItems()) {
                    String detailId = item.getId();

                    // 找到对应的 soDetailId
                    String soDetailId = detailSoDetailMap.get(detailId);
                    if (soDetailId != null) {
                        // 从映射中获取 platformSkuNo
                        String platformSkuNo = soDetailSkuMap.get(soDetailId);
                        if (platformSkuNo != null) {
                            item.setSkuNo(platformSkuNo);
                        }
                    }
                    item.setPlatformSkuNo(item.getWarehousePlatformSku());
                }
            }
        }
        req.setWarehouseOperationTypeDTOList(warehouseOperationTypeDTOList);
        req.setAuthId(overseasProviderEntity.getId());
        req.setThirdWarehouseProvideCode(overseasProviderEntity.getCode());
        req.setIsInsurance(false);
        req.setIsSignature(false);
        if (StrUtil.isNotBlank(entity.getLogisticsChannelId())) {
            LogisticsChannelEntity logisticsChannelEntity = logisticsFeign.getChannelById(entity.getLogisticsChannelId());
            if (Objects.nonNull(logisticsChannelEntity)) {
                req.setIsInsurance(Boolean.TRUE.equals(logisticsChannelEntity.getIsApiInsurance()));
                req.setIsSignature(Boolean.TRUE.equals(logisticsChannelEntity.getIsApiSign()));
            }
        }
        req.setOwnerCode(overseasProviderEntity.getOwnerCode());
        fillAttachmentInfo(req, attachmentList);
        return BeanUtil.beanToMap(req);
    }

    private void fillAttachmentInfo(ThirdWarehouseCreateFbaOutboundReq req, List<WmsAttachmentDTO.UpdateDTO> attachmentList) {
        if (CollUtil.isEmpty(attachmentList)) {
            return;
        }
        WmsAttachmentDTO.UpdateDTO attachment = attachmentList.get(0);
        if (Objects.isNull(attachment) || StrUtil.isBlank(attachment.getAttachUrl())) {
            return;
        }
        String fileName = attachment.getAttachName();
        byte[] bytes;
        try {
            // 任务调度层负责失败重试；这里下载失败直接抛出，避免继续推送缺失附件的三方仓单据。
            bytes = fileFeign.downloadFile(attachment.getAttachUrl());
        } catch (Exception e) {
            log.warn("B2B三方发货单附件下载异常，sourceId={}, fileUrl={}", req.getSourceId(), attachment.getAttachUrl(), e);
            saveAttachmentDownloadErrorMsg(req.getSourceId(), req.getReferenceNo(),
                    StrUtil.format("B2B三方仓附件下载异常，单号：{}，附件：{}", req.getReferenceNo(), attachment.getAttachName()));
            throw new ServiceException("B2B三方仓附件下载异常，单号：{}，附件：{}", req.getReferenceNo(), attachment.getAttachName());
        }
        if (Objects.isNull(bytes) || bytes.length == 0) {
            log.warn("B2B三方发货单附件下载为空，sourceId={}, fileUrl={}", req.getSourceId(), attachment.getAttachUrl());
            saveAttachmentDownloadErrorMsg(req.getSourceId(), req.getReferenceNo(),
                    StrUtil.format("B2B三方仓附件下载为空，单号：{}，附件：{}", req.getReferenceNo(), attachment.getAttachName()));
            throw new ServiceException("B2B三方仓附件下载为空，单号：{}，附件：{}", req.getReferenceNo(), attachment.getAttachName());
        }
        bytes = cleanAttachmentBytes(bytes, fileName, attachment.getAttachUrl(), req.getSourceId());
        req.setFileName(fileName);
        req.setFileUrl(FastDFSClientUtil.publicUrl + attachment.getAttachUrl());
        req.setFileBase64(Base64.getEncoder().encodeToString(bytes));
    }

    private byte[] cleanAttachmentBytes(byte[] bytes, String fileName, String fileUrl, String sourceId) {
        String extension = StrUtil.blankToDefault(FileUtil.getFileExtension(fileName), FileUtil.getFileExtension(fileUrl));
        if ("pdf".equalsIgnoreCase(extension)) {
            return trimLeadingBytes(bytes, new byte[]{'%', 'P', 'D', 'F', '-'}, fileName, sourceId);
        }
        if ("xlsx".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension)) {
            return trimLeadingBytes(bytes, new byte[]{'P', 'K'}, fileName, sourceId);
        }
        return bytes;
    }

    private byte[] trimLeadingBytes(byte[] bytes, byte[] magic, String fileName, String sourceId) {
        int index = indexOf(bytes, magic);
        if (index <= 0) {
            if (index < 0) {
                log.warn("B2B三方发货单附件文件头未匹配, sourceId={}, fileName={}", sourceId, fileName);
            }
            return bytes;
        }
        log.warn("B2B三方发货单附件存在前置脏字节，已裁剪, sourceId={}, fileName={}, offset={}", sourceId, fileName, index);
        return Arrays.copyOfRange(bytes, index, bytes.length);
    }

    private int indexOf(byte[] bytes, byte[] magic) {
        if (Objects.isNull(bytes) || Objects.isNull(magic) || bytes.length < magic.length) {
            return -1;
        }
        for (int i = 0; i <= bytes.length - magic.length; i++) {
            boolean matched = true;
            for (int j = 0; j < magic.length; j++) {
                if (bytes[i + j] != magic[j]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return i;
            }
        }
        return -1;
    }
    /**
     * @param operate
     * @param resultMap
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     */
    private DmpPushTaskEntity saveCreateTask(B2bThirdDeliveryEntity entity, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.B2B_THIRD_DELIVERY_CREATE.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        String msg = StrUtil.format("用户【{}】操作【{}】单据单号为【{}】异步创建三方仓出库订单", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单" , entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "【中台任务】创建三方仓出库订单");

        if (CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY_CREATE.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_B2B_THIRD_DELIVERY_CREATE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.ERP_B2B_THIRD_WAREHOUSE_CREATE_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }

        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY_CREATE.getCode());
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        wmsPushMsgEntity.setTargetPlatform(PlatformEnum.ERP.getName());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);

        return null;
    }


    @Override
    public Map<String, Object> newSyncDataToThirdWarehouseCancel(B2bThirdDeliveryEntity entity) {

        OverseasProviderEntity overseasProviderEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(entity.getDeliveryWarehouseId());
        if (Objects.isNull(overseasProviderEntity)){
            throw new ServiceException(ApiError.WH_OVERSEAS_PROVIDER_NOT_FOUND);
        }

        ThirdWarehouseCancelFbaOutboundReq req = new ThirdWarehouseCancelFbaOutboundReq();
        req.setAuthId(overseasProviderEntity.getId());
        req.setThirdWarehouseProvideCode(overseasProviderEntity.getCode());
        req.setErpOrderCode(entity.getCode());
        req.setOrderCode(entity.getPlatformOrderCode());
        req.setSourceId(entity.getId());
        req.setSourceCode(entity.getCode());
        req.setSoCode(entity.getSoCode());
        req.setRemark(entity.getRemark());
        req.setOwnerCode(overseasProviderEntity.getOwnerCode());
        return BeanUtil.beanToMap(req);
    }

    private void fillPackingForOutboundReq(ThirdWarehouseCreateFbaOutboundReq req, B2bThirdDeliveryEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getPackingType())) {
            req.setPackingType(B2bPackingTypeEnum.WAREHOUSE_SELF.getCode());
            return;
        }
        req.setPackingType(entity.getPackingType());
        List<B2bCustomerPackingEntity> packingList = b2bCustomerPackingService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollUtil.isEmpty(packingList)) {
            return;
        }
        Map<String, List<WmsAttachmentDTO.UpdateDTO>> shipmentFileMap = getShipmentFileMap(packingList);
        validateBoxHeadShipmentFiles(entity, packingList, shipmentFileMap);
        List<ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem> items = packingList.stream().map(p -> {
            ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem item = new ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem();
            item.setWarehousePlatformSku(p.getWarehousePlatformSku());
            item.setPackingQty(p.getPackingQty());
            item.setBoxMarkNo(p.getBoxMarkNo());
            item.setBoxMarkRefNo(p.getBoxMarkRefNo());
            item.setLabelSize(p.getLabelSize());
            fillShipmentFiles(item, shipmentFileMap.get(p.getId()), entity.getId(), entity.getCode());
            item.setLabelingRequirement(p.getLabelingRequirement());
            item.setBoxSeq(p.getBoxSeq());
            return item;
        }).collect(Collectors.toList());
        req.setPackingDetailList(items);
    }

    private void validateBoxHeadShipmentFiles(B2bThirdDeliveryEntity entity,
                                              List<B2bCustomerPackingEntity> packingList,
                                              Map<String, List<WmsAttachmentDTO.UpdateDTO>> shipmentFileMap) {
        Integer labelsPerBox = entity.getLabelsPerBox() != null ? entity.getLabelsPerBox() : 0;
        String packingType = CharSequenceUtil.blankToDefault(entity.getPackingType(), B2bPackingTypeEnum.WAREHOUSE_SELF.getCode());
        if (labelsPerBox <= 0 || !B2bPackingTypeEnum.requiresPackingDetail(packingType)) {
            return;
        }
        Set<Integer> boxSeqSet = packingList.stream()
                .map(B2bCustomerPackingEntity::getBoxSeq)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Integer boxSeq : boxSeqSet) {
            B2bCustomerPackingEntity boxHead = b2bCustomerPackingService.getBoxHead(packingList, boxSeq).orElse(null);
            if (boxHead == null || CollUtil.isNotEmpty(shipmentFileMap.get(boxHead.getId()))) {
                continue;
            }
            log.error("B2B三方发货单装箱标签附件缺失，箱序号：{}, mainId={}", boxSeq, entity.getId());
            throw new ServiceException("装箱标签附件缺失，箱序号：{}", boxSeq);
        }
    }

    private Map<String, List<WmsAttachmentDTO.UpdateDTO>> getShipmentFileMap(List<B2bCustomerPackingEntity> packingList) {
        List<String> boxHeadIds = packingList.stream()
                .map(B2bCustomerPackingEntity::getBoxSeq)
                .filter(Objects::nonNull)
                .distinct()
                .map(boxSeq -> b2bCustomerPackingService.getBoxHead(packingList, boxSeq).orElse(null))
                .filter(Objects::nonNull)
                .map(B2bCustomerPackingEntity::getId)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(boxHeadIds)) {
            return Collections.emptyMap();
        }
        List<WmsAttachmentDTO.UpdateDTO> attachments = wmsAttachmentService.getByBusinessIds(boxHeadIds, ModuleTypeEnum.B2B_CUSTOMER_PACKING_LABEL.getCode());
        if (CollUtil.isEmpty(attachments)) {
            return Collections.emptyMap();
        }
        return attachments.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getBusinessId()) && CharSequenceUtil.isNotBlank(e.getAttachUrl()))
                .collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getBusinessId, LinkedHashMap::new, Collectors.toList()));
    }

    private void fillShipmentFiles(ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem item,
                                   List<WmsAttachmentDTO.UpdateDTO> attachments,
                                   String sourceId,
                                   String sourceCode) {
        if (CollUtil.isEmpty(attachments)) {
            return;
        }
        List<ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem> shipmentFiles = new ArrayList<>(attachments.size());
        for (WmsAttachmentDTO.UpdateDTO attachment : attachments) {
            ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem shipmentFile = buildShipmentFile(item, attachment, sourceId, sourceCode);
            if (Objects.isNull(shipmentFile)) {
                throw new ServiceException("B2B三方发货单装箱标签附件构建结果不能为空，箱序号：{}", item.getBoxSeq());
            }
            shipmentFiles.add(shipmentFile);
        }
        if (CollUtil.isEmpty(shipmentFiles)) {
            return;
        }
        ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem firstShipmentFile = shipmentFiles.get(0);
        item.setShipmentFileUrl(firstShipmentFile.getShipmentFileUrl());
        item.setShipmentFileName(firstShipmentFile.getShipmentFileName());
        item.setShipmentFileBase64(firstShipmentFile.getShipmentFileBase64());
        item.setShipmentFileList(shipmentFiles);
    }

    private ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem buildShipmentFile(ThirdWarehouseCreateFbaOutboundReq.PackingDetailItem item,
                                                                                  WmsAttachmentDTO.UpdateDTO attachment,
                                                                                  String sourceId,
                                                                                  String sourceCode) {
        if (Objects.isNull(attachment) || CharSequenceUtil.isBlank(attachment.getAttachUrl())) {
            throw new ServiceException("B2B三方发货单装箱标签附件缺失，箱序号：{}", item.getBoxSeq());
        }
        byte[] bytes;
        try {
            // 任务调度层负责失败重试；这里下载失败直接抛出，避免继续推送缺失标签的三方仓单据。
            bytes = fileFeign.downloadFile(attachment.getAttachUrl());
        } catch (Exception e) {
            log.warn("B2B三方发货单装箱标签附件下载异常，boxSeq={}, fileUrl={}", item.getBoxSeq(), attachment.getAttachUrl(), e);
            saveAttachmentDownloadErrorMsg(sourceId, sourceCode,
                    StrUtil.format("B2B三方发货单装箱标签附件下载异常，箱序号：{}，附件：{}", item.getBoxSeq(), attachment.getAttachName()));
            throw new ServiceException("B2B三方发货单装箱标签附件下载异常，箱序号：{}，附件：{}", item.getBoxSeq(), attachment.getAttachName());
        }
        if (Objects.isNull(bytes) || bytes.length == 0) {
            log.warn("B2B三方发货单装箱标签附件下载为空，boxSeq={}, fileUrl={}", item.getBoxSeq(), attachment.getAttachUrl());
            saveAttachmentDownloadErrorMsg(sourceId, sourceCode,
                    StrUtil.format("B2B三方发货单装箱标签附件下载为空，箱序号：{}，附件：{}", item.getBoxSeq(), attachment.getAttachName()));
            throw new ServiceException("B2B三方发货单装箱标签附件下载为空，箱序号：{}，附件：{}", item.getBoxSeq(), attachment.getAttachName());
        }
        bytes = cleanAttachmentBytes(bytes, attachment.getAttachName(), attachment.getAttachUrl(), item.getBoxSeq() == null ? "" : String.valueOf(item.getBoxSeq()));
        return ThirdWarehouseCreateFbaOutboundReq.ShipmentFileItem.builder()
                .shipmentFileUrl(FastDFSClientUtil.publicUrl + attachment.getAttachUrl())
                .shipmentFileName(attachment.getAttachName())
                .shipmentFileBase64(Base64.getEncoder().encodeToString(bytes))
                .build();
    }

    /**
     * 附件下载失败时写入本地补偿消息，便于运维人工介入；写入失败不影响主流程异常抛出。
     */
    private void saveAttachmentDownloadErrorMsg(String sourceId, String sourceCode, String remark) {
        if (CharSequenceUtil.isBlank(sourceId)) {
            return;
        }
        try {
            WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
            wmsPushMsgEntity.setSourceId(sourceId);
            wmsPushMsgEntity.setSourceCode(sourceCode);
            wmsPushMsgEntity.setSourceType(SourceTypeEnum.B2B_THIRD_DELIVERY_CREATE.getCode());
            wmsPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_SYNC_ERROR.getCode());
            wmsPushMsgEntity.setTargetPlatform(PlatformEnum.ERP.getName());
            Map<String, String> pushData = new HashMap<>();
            pushData.put("remark", remark);
            wmsPushMsgEntity.setPushData(JSON.toJSONString(pushData));
            wmsPushMsgService.save(wmsPushMsgEntity);
        } catch (Exception e) {
            log.warn("B2B三方发货单附件下载失败补偿消息写入失败, sourceId={}", sourceId, e);
        }
    }
}
