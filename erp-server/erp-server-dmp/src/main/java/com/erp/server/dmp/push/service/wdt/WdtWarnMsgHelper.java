package com.erp.server.dmp.push.service.wdt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 旺店通推送告警消息发送公共类。
 * <p>抽取自 WdtOtherInStockServiceImpl / WdtOtherOutStockServiceImpl 中重复的告警发送逻辑，
 * 统一字段组装、字典 key、MQ 发送以及异常吞噬策略，避免 In/Out 两个实现散落同样的代码。</p>
 *
 * @author refactor
 */
@Slf4j
@Component
public class WdtWarnMsgHelper {

    /** 接收用户列表的字典 key（对应实施组通知人员） */
    private static final String DICT_USER_KEY = "wdtUpdateInventoryUser";
    /** 告警绑定的业务表名 */
    private static final String TABLE_NAME = "dmp_push_task";

    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private DictBasicService dictBasicService;

    /**
     * 发送旺店通告警 MQ：发送过程中的任何异常都被吞噬并打 warn 日志，
     * 调用方主流程的业务异常不会被覆盖。
     *
     * @param bizName  bizName 业务名（如：旺店通其他入库单同步通知）
     * @param title    告警标题
     * @param tableId  关联业务表主键（推荐传 dmpSyncTaskId，为空时回退到 sourceId）
     * @param keyInfo  告警关键信息（详细错误描述）
     */
    public void safeSendCreateOrApproveWarnMsg(String bizName, String title, String tableId, String keyInfo) {
        try {
            sendCreateOrApproveWarnMsg(bizName, title, tableId, keyInfo);
        } catch (Exception e) {
            log.warn("发送旺店通告警 MQ 失败，不影响主流程：bizName={}, title={}, keyInfo={}", bizName, title, keyInfo, e);
        }
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

    /**
     * tableId 兜底：优先 dmpSyncTaskId，回退 sourceId。
     */
    public static String resolveTableId(String dmpSyncTaskId, String sourceId) {
        return CharSequenceUtil.isNotBlank(dmpSyncTaskId) ? dmpSyncTaskId : sourceId;
    }
}
