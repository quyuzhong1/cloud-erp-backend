package com.erp.server.dmp.push.service.wdt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpPushWdtEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.dmp.push.service.wdt.dto.OtherStockWarnContext;
import com.erp.server.dmp.push.service.wdt.dto.ResolvedWarnFields;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.service.DmpPushWdtService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.sdk.wangdian.sdk.WdtErpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 旺店通推送告警消息发送公共类。
 * <p>抽取自 WdtOtherInStockServiceImpl / WdtOtherOutStockServiceImpl 的告警发送逻辑，
 * 统一字段组装、中台仓库映射解析、MQ 发送及异常吞噬策略；告警发送失败不影响主流程抛错。</p>
 */
@Slf4j
@Component
public class WdtWarnMsgHelper {

    private static final String DICT_USER_KEY = "wdtUpdateInventoryUser";
    private static final String TABLE_NAME = "dmp_push_task";
    /** 无法解析单据类型时的兜底名称 */
    private static final String DEFAULT_DOC_TYPE_NAME = "旺店通其他出入库单";

    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private DmpPushWdtService dmpPushWdtService;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;

    public void safeSendOtherStockCreateFailWarn(OtherStockWarnContext context, Integer wdtErrorCode, String wdtErrorMsg) {
        try {
            ResolvedWarnFields fields = resolveWarnFields(context);
            String keyInfo = buildCreateFailMsg(fields.getDocTypeName(), fields.getErpSourceCode(), fields.getOuterNo(),
                    fields.getErpWarehouseName(), fields.getWdtWarehouseNo(), wdtErrorCode, wdtErrorMsg);
            String title = CharSequenceUtil.format("创建{}失败", fields.getDocTypeName());
            safeSendCreateOrApproveWarnMsg(fields.getBizName(), title, fields.getTableId(), keyInfo);
        } catch (Exception e) {
            log.warn("发送旺店通其他出入库创建失败告警失败，不影响主流程：context={}, wdtErrorCode={}, wdtErrorMsg={}",
                    context, wdtErrorCode, wdtErrorMsg, e);
        }
    }

    public void safeSendOtherStockAuditFailWarn(OtherStockWarnContext context, Integer wdtErrorCode, String wdtErrorMsg) {
        try {
            ResolvedWarnFields fields = resolveWarnFields(context);
            String keyInfo = buildAuditFailMsg(fields.getDocTypeName(), fields.getErpSourceCode(), fields.getErpWarehouseName(),
                    fields.getOuterNo(), fields.getWdtWarehouseNo(), wdtErrorCode, wdtErrorMsg);
            String title = CharSequenceUtil.format("审核{}失败", fields.getDocTypeName());
            safeSendCreateOrApproveWarnMsg(fields.getBizName(), title, fields.getTableId(), keyInfo);
        } catch (Exception e) {
            log.warn("发送旺店通其他出入库审核失败告警失败，不影响主流程：context={}, wdtErrorCode={}, wdtErrorMsg={}",
                    context, wdtErrorCode, wdtErrorMsg, e);
        }
    }

    /**
     * 旺店通异常信息拼装，货位不存在时附加受影响 SKU。
     */
    public static <T> String buildWdtExceptionMessage(WdtErpException e, List<T> goodsList,
                                                    Function<T, String> positionNoGetter, Function<T, String> specNoGetter) {
        String rawMsg = e.getMessage();
        StringBuilder message = new StringBuilder(CharSequenceUtil.nullToEmpty(rawMsg));
        if (CharSequenceUtil.isNotBlank(rawMsg) && rawMsg.contains("货位不存在")
                && CollUtil.isNotEmpty(goodsList)) {
            String positionNo = rawMsg.replace("货位不存在", "").trim();
            if (CharSequenceUtil.isNotBlank(positionNo)) {
                List<String> skuList = goodsList.stream()
                        .filter(item -> Objects.equals(positionNoGetter.apply(item), positionNo))
                        .map(specNoGetter)
                        .distinct()
                        .collect(Collectors.toList());
                message.append("，受影响SKU：").append(String.join(",", skuList));
            }
        }
        return message.toString();
    }

    public static String buildCreateFailMsg(String docTypeName, String erpSourceCode, String wdtOrderNo, String erpWarehouseName,
                                            String wdtWarehouseNo, Integer wdtErrorCode, String wdtErrorMsg) {
        return CharSequenceUtil.format("创建{}失败，数大臣单号：{}，旺店通单号：{}，数大臣仓库：{}，旺店通仓库编码：{}，旺店通错误码：{}，错误信息：{}",
                docTypeName,
                CharSequenceUtil.nullToEmpty(erpSourceCode),
                CharSequenceUtil.nullToEmpty(wdtOrderNo),
                CharSequenceUtil.nullToEmpty(erpWarehouseName),
                CharSequenceUtil.nullToEmpty(wdtWarehouseNo),
                wdtErrorCode == null ? "" : wdtErrorCode,
                CharSequenceUtil.nullToEmpty(wdtErrorMsg));
    }

    public static String buildAuditFailMsg(String docTypeName, String erpSourceCode, String erpWarehouseName,
                                           String wdtOrderNo, String wdtWarehouseNo, Integer wdtErrorCode, String wdtErrorMsg) {
        return CharSequenceUtil.format("审核{}失败，数大臣单号：{}，数大臣仓库：{}，旺店通单号：{}，旺店通仓库编码：{}，旺店通错误码：{}，错误信息：{}",
                docTypeName,
                CharSequenceUtil.nullToEmpty(erpSourceCode),
                CharSequenceUtil.nullToEmpty(erpWarehouseName),
                CharSequenceUtil.nullToEmpty(wdtOrderNo),
                CharSequenceUtil.nullToEmpty(wdtWarehouseNo),
                wdtErrorCode == null ? "" : wdtErrorCode,
                CharSequenceUtil.nullToEmpty(wdtErrorMsg));
    }

    public void safeSendCreateOrApproveWarnMsg(String bizName, String title, String tableId, String keyInfo) {
        try {
            sendCreateOrApproveWarnMsg(bizName, title, tableId, keyInfo);
        } catch (Exception e) {
            log.warn("发送旺店通告警 MQ 失败，不影响主流程：bizName={}, title={}, keyInfo={}", bizName, title, keyInfo, e);
        }
    }

    /**
     * tableId 兜底：优先 dmpSyncTaskId，回退 sourceId。
     */
    public static String resolveTableId(String dmpSyncTaskId, String sourceId) {
        return CharSequenceUtil.isNotBlank(dmpSyncTaskId) ? dmpSyncTaskId : CharSequenceUtil.nullToEmpty(sourceId);
    }

    private ResolvedWarnFields resolveWarnFields(OtherStockWarnContext context) {
        ResolvedWarnFields fields = new ResolvedWarnFields();
        fields.setTableId(resolveTableId(context.getDmpSyncTaskId(), context.getSourceId()));
        fields.setOuterNo(CharSequenceUtil.nullToEmpty(context.getOuterNo()));
        fields.setErpSourceCode(resolveErpSourceCode(context));
        ApiModuleTypeEnum apiModuleType = context.getApiModuleType();
        String docTypeName = apiModuleType != null ? apiModuleType.getDesc() : DEFAULT_DOC_TYPE_NAME;
        fields.setDocTypeName(docTypeName);
        fields.setBizName(CharSequenceUtil.format("{}同步通知", docTypeName));

        String sysWarehouseId = resolveSysWarehouseId(context);
        if (CharSequenceUtil.isNotBlank(sysWarehouseId)) {
            fields.setErpWarehouseName(resolveErpWarehouseName(sysWarehouseId));
            fields.setWdtWarehouseNo(resolveWdtWarehouseCode(sysWarehouseId, context.getWdtWarehouseNo()));
        } else {
            fields.setWdtWarehouseNo(CharSequenceUtil.nullToEmpty(context.getWdtWarehouseNo()));
        }
        return fields;
    }

    private String resolveErpWarehouseName(String sysWarehouseId) {
        try {
            List<WarehouseDTO.ListDTO> warehouseList = wmsWarehouseFeign.listByIds(Collections.singletonList(sysWarehouseId));
            if (CollUtil.isNotEmpty(warehouseList) && CharSequenceUtil.isNotBlank(warehouseList.get(0).getName())) {
                return warehouseList.get(0).getName();
            }
        } catch (Exception e) {
            log.warn("查询ERP仓库名称失败，使用仓库ID兜底: sysWarehouseId={}", sysWarehouseId, e);
        }
        return sysWarehouseId;
    }

    private String resolveWdtWarehouseCode(String sysWarehouseId, String fallbackWdtWarehouseNo) {
        try {
            List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = thirdMappingService.listMappingBySysIds(
                    Collections.singletonList(sysWarehouseId), ThirdSysTypeEnum.WDT.getCode());
            if (CollUtil.isNotEmpty(mappingList) && CharSequenceUtil.isNotBlank(mappingList.get(0).getThirdWarehouseCode())) {
                return mappingList.get(0).getThirdWarehouseCode();
            }
        } catch (Exception e) {
            log.warn("从中台配置查询旺店通仓库编码失败，使用请求值兜底: sysWarehouseId={}", sysWarehouseId, e);
        }
        return CharSequenceUtil.nullToEmpty(fallbackWdtWarehouseNo);
    }

    private String resolveErpSourceCode(OtherStockWarnContext context) {
        if (CharSequenceUtil.isNotBlank(context.getSourceCode())) {
            return context.getSourceCode();
        }
        if (CharSequenceUtil.isNotBlank(context.getDmpSyncTaskId())) {
            try {
                DmpPushTaskEntity pushTask = dmpPushTaskService.getById(context.getDmpSyncTaskId());
                if (pushTask != null && CharSequenceUtil.isNotBlank(pushTask.getSourceCode())) {
                    return pushTask.getSourceCode();
                }
                if (pushTask != null && CharSequenceUtil.isNotBlank(pushTask.getSourceId())) {
                    DmpPushWdtEntity pushWdtEntity = dmpPushWdtService.getById(pushTask.getSourceId());
                    if (pushWdtEntity != null && CharSequenceUtil.isNotBlank(pushWdtEntity.getSourceCode())) {
                        return pushWdtEntity.getSourceCode();
                    }
                }
            } catch (Exception e) {
                log.warn("通过推送任务反查ERP单号失败: dmpSyncTaskId={}", context.getDmpSyncTaskId(), e);
            }
        }
        return "";
    }

    private String resolveSysWarehouseId(OtherStockWarnContext context) {
        if (CharSequenceUtil.isNotBlank(context.getSysWarehouseId())) {
            return context.getSysWarehouseId();
        }
        if (CharSequenceUtil.isNotBlank(context.getDmpSyncTaskId())) {
            try {
                DmpPushTaskEntity pushTask = dmpPushTaskService.getById(context.getDmpSyncTaskId());
                if (pushTask != null && CharSequenceUtil.isNotBlank(pushTask.getSourceId())) {
                    DmpPushWdtEntity pushWdtEntity = dmpPushWdtService.getById(pushTask.getSourceId());
                    if (pushWdtEntity != null && CharSequenceUtil.isNotBlank(pushWdtEntity.getWarehouseId())) {
                        return pushWdtEntity.getWarehouseId();
                    }
                }
            } catch (Exception e) {
                log.warn("通过推送任务反查ERP仓库失败: dmpSyncTaskId={}", context.getDmpSyncTaskId(), e);
            }
        }
        if (CharSequenceUtil.isNotBlank(context.getWdtWarehouseNo())) {
            try {
                ThirdMappingEntity mapping = thirdMappingService.getByThirdCodeAndType(
                        context.getWdtWarehouseNo(), ThirdSysTypeEnum.WDT.getCode(), ThirdSysTypeEnum.WAREHOUSE.getCode());
                if (mapping != null && CharSequenceUtil.isNotBlank(mapping.getSysId())) {
                    return mapping.getSysId();
                }
            } catch (Exception e) {
                log.warn("通过旺店通仓库编码反查ERP仓库失败: wdtWarehouseNo={}", context.getWdtWarehouseNo(), e);
            }
        }
        return null;
    }

    private void sendCreateOrApproveWarnMsg(String bizName, String title, String tableId, String keyInfo) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(bizName);
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTitle(title);
        warnMsgInfo.setTableName(TABLE_NAME);
        warnMsgInfo.setTableId(CharSequenceUtil.nullToEmpty(tableId));
        warnMsgInfo.setKeyInfo(CharSequenceUtil.nullToEmpty(keyInfo));
        List<DictBasicEntity> viewDTOList = dictBasicService.getByKey(DICT_USER_KEY);
        warnMsgInfo.setUserIdList(CollUtil.isNotEmpty(viewDTOList)
                ? viewDTOList.stream().map(DictBasicEntity::getValue).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList())
                : new ArrayList<>());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
}
