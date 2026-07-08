package com.erp.server.dmp.push.service.wdt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.push.service.wdt.dto.OtherStockWarnContext;
import com.erp.server.dmp.push.service.wdt.dto.ResolvedWarnFields;
import com.erp.server.dmp.service.DictBasicService;
import com.sdk.wangdian.sdk.WdtErpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 旺店通推送告警消息发送公共类。
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

    public void safeSendOtherStockCreateFailWarn(OtherStockWarnContext context, Integer wdtErrorCode, String wdtErrorMsg) {
        try {
            ResolvedWarnFields fields = resolveWarnFields(context);
            String keyInfo = buildCreateFailMsg(fields.getDocTypeName(), fields.getOuterNo(), fields.getWdtWarehouseNo(),
                    wdtErrorCode, wdtErrorMsg);
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
            String keyInfo = buildAuditFailMsg(fields.getDocTypeName(), fields.getOuterNo(), fields.getWdtWarehouseNo(),
                    context.getOuterNo(), context.getWdtWarehouseNo(), wdtErrorCode, wdtErrorMsg);
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

    public static String buildCreateFailMsg(String docTypeName, String outerNo, String wdtWarehouseNo,
                                            Integer wdtErrorCode, String wdtErrorMsg) {
        return CharSequenceUtil.format("创建{}失败，数大臣单号：{}，数大臣仓库编码：{}，旺店通错误码：{}，错误信息：{}",
                docTypeName,
                CharSequenceUtil.nullToEmpty(outerNo),
                CharSequenceUtil.nullToEmpty(wdtWarehouseNo),
                wdtErrorCode == null ? "" : wdtErrorCode,
                CharSequenceUtil.nullToEmpty(wdtErrorMsg));
    }

    public static String buildAuditFailMsg(String docTypeName, String outerNo, String erpWarehouseNo,
                                           String wdtOrderNo, String wdtWarehouseNo, Integer wdtErrorCode, String wdtErrorMsg) {
        return CharSequenceUtil.format("审核{}失败，数大臣单号：{}，数大臣仓库编码：{}，旺店通单号：{}，旺店通仓库编码：{}，旺店通错误码：{}，错误信息：{}",
                docTypeName,
                CharSequenceUtil.nullToEmpty(outerNo),
                CharSequenceUtil.nullToEmpty(erpWarehouseNo),
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
        fields.setWdtWarehouseNo(CharSequenceUtil.nullToEmpty(context.getWdtWarehouseNo()));
        ApiModuleTypeEnum apiModuleType = context.getApiModuleType();
        String docTypeName = apiModuleType != null ? apiModuleType.getDesc() : DEFAULT_DOC_TYPE_NAME;
        fields.setDocTypeName(docTypeName);
        fields.setBizName(CharSequenceUtil.format("{}同步通知", docTypeName));
        return fields;
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
