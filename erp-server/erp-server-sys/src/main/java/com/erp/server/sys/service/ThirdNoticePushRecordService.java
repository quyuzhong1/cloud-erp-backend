package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.CfgApproveSyncFieldMapEntity;
import com.erp.model.sys.entity.CfgRuleConditionEntity;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 三方通知推送记录 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-26
 */
public interface ThirdNoticePushRecordService extends SuperService<ThirdNoticePushRecordEntity> {
    PagingVO<ThirdNoticePushRecordDTO.ListDTO> paging(PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> dto);

    void exportList(ThirdNoticePushRecordDTO.PagingParamDTO dto, HttpServletResponse response);

    List<ThirdNoticePushRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    Boolean insertBatch(List<ThirdNoticePushRecordEntity> list);

    BatchResultDTO repush(String id);

    void sendThirdNoticeByMqAsync(Map<String, Object> jsonMap, List<String> diffFields);

    MqConsumerRecordDTO.MqDTO buildMqRecordDTO(Map<String, Object> jsonMap,
                                               List<String> diffFields,
                                               String businessKey);

    String getBusinessKeyWithCache(String table);

    Map<String, Object> convertToCamelCaseMap(Map<String, Object> jsonMap);

    void sendThirdNoticeByMq(MqConsumerRecordDTO.MqDTO dto);

    void sendMsgByCfg(MqConsumerRecordDTO.MqDTO dto, CfgThirdNoticeEntity noticeEntity, List<CfgRuleConditionEntity> ruleList, String bussinessKey, List<CfgApproveSyncFieldMapEntity> fieldList, List<CfgQueryOptionEntity> cfgQueryOptionList);

    void saveFailedRecordByType(MqConsumerRecordDTO.MqDTO dto, CfgThirdNoticeEntity noticeEntity, String noticeMethod, String businessId, String bussinessKey, String errorReason, String code, String thirdNoticePushFailedType);

    boolean checkRule(MqConsumerRecordDTO.MqDTO dto, CfgThirdNoticeEntity noticeEntity, List<CfgRuleConditionEntity> cfgRuleConditionEntities, String bussinessKey);

    List<String> getUserList(String post, String roleType, String specificPerson, String businessId, String businessKey);

    List<ThirdNoticePushRecordEntity> listSendingRecord(ThirdNoticePushRecordDTO.ParamsDTO paramsDTO);

    void sendThirdNoticeJob();

    void handleSendNoticeFailedType(MqConsumerRecordDTO.MqDTO dto, ThirdNoticePushRecordEntity entity, Map<String, Object> dataJson);

    void handleNoPersonFailedType(MqConsumerRecordDTO.MqDTO dto, ThirdNoticePushRecordEntity entity, Map<String, Object> dataJson);
}
