package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.constant.NoticeMsgConstant;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.*;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.enums.ReplenishBillStatusEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.CfgQueryOptionExtendTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.rpc.tms.feign.TmsProductRegistrationFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.mapper.ThirdNoticePushRecordMapper;
import com.erp.server.sys.service.*;
import com.google.gson.Gson;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.redisson.executor.CronExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE_RECORD;
import static com.erp.server.sys.rocketmq.consumer.MqRecordConsumerService.TABLE_BUSINESS_KEY;

/**
 * <p>
 * 三方通知推送记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-26
 */
@Slf4j
@Service
public class ThirdNoticePushRecordServiceImpl extends SuperServiceImpl<ThirdNoticePushRecordMapper, ThirdNoticePushRecordEntity> implements ThirdNoticePushRecordService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FsService fsService;
    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;
    @Resource
    private SysPostFeign sysPostFeign;
    @Resource
    private TmsProductRegistrationFeign tmsProductRegistrationFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private DictNoticeRoleOptionService dictNoticeRoleOptionService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private SpElServer spElServer;
    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private CfgRuleConditionService cfgRuleConditionService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private MqConsumerRecordService mqConsumerRecordService;

    @Override
    public List<ThirdNoticePushRecordDTO.TabListDTO> tabList(PermissionsDTO param) {
        ThirdNoticePushRecordDTO.PagingParamDTO searchParam = new ThirdNoticePushRecordDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ThirdNoticePushRecordDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<ThirdNoticePushRecordDTO.TabListDTO> result = new ArrayList<>();
        ThirdNoticePushRecordDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals(ThirdNoticePushRecordStatusEnum.SUCCESS.getCode())).findFirst().orElse(null);
        ThirdNoticePushRecordDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals(ThirdNoticePushRecordStatusEnum.FAILED.getCode())).findFirst().orElse(null);
        result.add(new ThirdNoticePushRecordDTO.TabListDTO("all", "全部" , 0));
        result.add(new ThirdNoticePushRecordDTO.TabListDTO(ThirdNoticePushRecordStatusEnum.SUCCESS.getCode(),ThirdNoticePushRecordStatusEnum.SUCCESS.getName(), null == enable ? 0 : enable.getCount()));
        result.add(new ThirdNoticePushRecordDTO.TabListDTO(ThirdNoticePushRecordStatusEnum.FAILED.getCode(),ThirdNoticePushRecordStatusEnum.FAILED.getName(), null == disable ? 0 : disable.getCount()));
        return result;
    }


    @Override
    public PagingVO<ThirdNoticePushRecordDTO.ListDTO> paging(PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ThirdNoticePushRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<ThirdNoticePushRecordDTO.ListDTO> records) {

        List<DictBasicDTO.ViewDTO> viewDTOS = dictBasicService.listByType("thirdNoticeBusinessType");

        Map<String, String> map = viewDTOS.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName, (o1, o2) -> o1));

        for (ThirdNoticePushRecordDTO.ListDTO record : records) {

            //单据类型
            String businessType = record.getBusinessType();
            record.setBusinessTypeName(map.get(businessType));

            record.setNoticeTypeName(ThirdNoticePushRecordNoticeTypeEnum.getName(record.getNoticeType()));

            record.setNoticeMethodName(CfgApproveSyncSyncPlatformEnum.getName(record.getNoticeMethod()));

            record.setStatusName(ThirdNoticePushRecordStatusEnum.getName(record.getStatus()));
        }
    }


    @Override
    public void exportList(ThirdNoticePushRecordDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("三方通知推送记录导出", EXPORT_SYS_THIRD_NOTICE_RECORD.getCode(), param);
    }

    /**
     * 批量插入推送记录
     * @author jack
     * @date 2025-05-30
     */
    @Override
    public Boolean insertBatch(List<ThirdNoticePushRecordEntity> list) {
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        Map<String, String> userMap = sysUserFeign.getUserListByUserIds(
                list.stream()
                        .map(ThirdNoticePushRecordEntity::getReceiverId)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName, (o1, o2) -> o1));

        list.forEach(entity -> entity.setReceiverName(userMap.getOrDefault(entity.getReceiverId(), "")));
        return baseMapper.insertBatch(list);
    }

    /**
     * 根据推送记录id 进行重推
     * @author jack
     * @date 2025-05-30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO repush(String id) {
        ThirdNoticePushRecordEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方通知推送记录数据"));
        if(entity.getStatus().equals(ThirdNoticePushRecordStatusEnum.SUCCESS.getCode())){
            return BatchResultDTO.fail(entity.getId(), entity.getId(), "重推仅限推送失败的记录");
        }

        Map<String, Object> dataJson = entity.getDataJson();
        if (CollUtil.isEmpty(dataJson)) {
            return BatchResultDTO.fail(entity.getId(), entity.getId(), "重推失败");
        }

        String failedType = String.valueOf(dataJson.get("thirdNoticePushFailedType"));
        if (StringUtils.isBlank(failedType)) {
            return BatchResultDTO.fail(entity.getId(), entity.getId(), "重推类型不存在");
        }

        //获取mq实体类
        Gson gson = new Gson();
        MqConsumerRecordDTO.MqDTO dto = gson.fromJson(gson.toJson(dataJson), MqConsumerRecordDTO.MqDTO.class);
        ThirdNoticePushFailedTypeEnum typeEnum = ThirdNoticePushFailedTypeEnum.getByCode(failedType);
        switch (typeEnum) {
            case ALL:
                sendThirdNoticeByMq(dto);
                break;
            case NOPERSON:
                handleNoPersonFailedType(dto, entity, dataJson);
                break;
            case SENDNOTICE:
                handleSendNoticeFailedType(dto, entity, dataJson);
                break;
            default:
                return BatchResultDTO.fail(entity.getId(), entity.getId(), "未知重推类型");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), "执行成功");
    }

    @Override
    public void handleNoPersonFailedType(MqConsumerRecordDTO.MqDTO dto, ThirdNoticePushRecordEntity entity, Map<String, Object> dataJson) {
        String cfgThirdNoticeId = String.valueOf(dataJson.get("cfgThirdNoticeId"));
        CfgThirdNoticeEntity noticeEntity = cfgThirdNoticeService.getById(cfgThirdNoticeId);
        if (Objects.isNull(noticeEntity)) {
            throw new ServiceException("通知配置不存在");
        }

        //根据参数判断一下通知的单据类型
        CfgQueryOptionDTO.MqParamsDTO mqParamsDTO = new CfgQueryOptionDTO.MqParamsDTO();
        mqParamsDTO.setTableName(dto.getTable());
        String[] split = dto.getDb().split("-");
        String sysClassify = split[split.length - 1];
        mqParamsDTO.setSysClassify(sysClassify);
        //查询出common 、表头、明细的配置
        List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionFeign.listByMqParams(mqParamsDTO);
        if (CollUtil.isEmpty(cfgQueryOptionList)) {
            throw new ServiceException("通知配置不存在");
        }

        String businessKey = cfgQueryOptionList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getBussinessKey()))
                .findFirst()
                .map(CfgQueryOptionEntity::getBussinessKey)
                .orElseThrow(() -> new ServiceException("单据类型不存在"));

        List<CfgApproveSyncFieldMapEntity> fieldList = cfgApproveSyncFieldMapService.lambdaQuery()
                .eq(CfgApproveSyncFieldMapEntity::getMainId, cfgThirdNoticeId)
                .list();

        List<CfgRuleConditionEntity> ruleList = cfgRuleConditionService.lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, cfgThirdNoticeId)
                .list();

        //校验规则条件
        if (!checkRule(dto, noticeEntity, ruleList, businessKey)) {
            throw new ServiceException("规则条件不满足");
        }
        sendMsgByCfg(dto, noticeEntity, ruleList, businessKey, fieldList, cfgQueryOptionList);
    }

    @Override
    public void handleSendNoticeFailedType(MqConsumerRecordDTO.MqDTO dto, ThirdNoticePushRecordEntity entity, Map<String, Object> dataJson) {
        String cfgThirdNoticeId = String.valueOf(dataJson.get("cfgThirdNoticeId"));
        CfgThirdNoticeEntity noticeEntity = cfgThirdNoticeService.getById(cfgThirdNoticeId);
        if (Objects.isNull(noticeEntity)) {
            throw new ServiceException("通知配置不存在");
        }

        List<ThirdUnionDTO> unionList = sysUserFeign.getThirdByUserIds(
                ThirdpartyPlatformEnum.FS.getCode(),
                Collections.singletonList(entity.getReceiverId())
        );
        Map<String, ThirdUnionDTO> unionMap = unionList.stream()
                .collect(Collectors.toMap(ThirdUnionDTO::getUserId, e -> e, (o1, o2) -> o1));

        ThirdUnionDTO unionDTO = unionMap.get(entity.getReceiverId());
        if (Objects.isNull(unionDTO) || StringUtils.isBlank(unionDTO.getThirdUnionId())) {
            throw new ServiceException("重推失败");
        }

        SendThirdNoticeConsumerDTO sendMessage = new SendThirdNoticeConsumerDTO();
        sendMessage.setUnionIds(Collections.singletonList(unionDTO.getThirdUnionId()));
        sendMessage.setContentMap(dataJson);

        if (Boolean.TRUE.equals(fsService.sendMessage(sendMessage))) {
            lambdaUpdate()
                    .set(ThirdNoticePushRecordEntity::getStatus, ThirdNoticePushRecordStatusEnum.SUCCESS.getCode())
                    .set(ThirdNoticePushRecordEntity::getSendTime, LocalDateTime.now())
                    .set(ThirdNoticePushRecordEntity::getErrorReason, "")
                    .eq(ThirdNoticePushRecordEntity::getId, entity.getId())
                    .update();
        }
    }

    /**
     * mq消费发送消息（即时推送）
     * @author jack
     * @date 2025-05-30
     */
    @Async("thirdNoticePushExecutor")
    @Override
    public void sendThirdNoticeByMqAsync(Map<String, Object> jsonMap, List<String> diffFields) {
        //参数校验
        if (MapUtils.isEmpty(jsonMap) || CollectionUtils.isEmpty(diffFields)) {
            log.error("MQ消息处理中止 - 参数不合法 jsonMap:{}, diffFields:{}", jsonMap, diffFields);
            return;
        }
        //根据table获取业务单据类型（带缓存）
        String table = jsonMap.get("table") == null ? "" : String.valueOf(jsonMap.get("table"));
        String businessKey = getBusinessKeyWithCache(table);
        if (StringUtils.isBlank(businessKey)) {
            log.error("未找到table[{}]对应的业务类型", table);
            return;
        }

        //检查是否存在有效配置
        List<CfgThirdNoticeEntity> cfgThirdNoticeList = cfgThirdNoticeService.lambdaQuery()
                .eq(CfgThirdNoticeEntity::getBusinessType, businessKey)
                .eq(CfgThirdNoticeEntity::getMethod, CfgThirdNoticeMethodEnum.SINGLE.getCode())
                .eq(CfgThirdNoticeEntity::getNoticeStatus, Boolean.TRUE)
                .list();
        if (CollUtil.isEmpty(cfgThirdNoticeList)) {
            log.error("业务类型[{}]无有效通知配置", businessKey);
            return;
        }

        //构建MQ记录DTO
        MqConsumerRecordDTO.MqDTO dto = buildMqRecordDTO(jsonMap, diffFields, businessKey);
        if (Objects.isNull(dto)) {
            return;
        }

        // //保存mq消费记录
        String id = mqConsumerRecordService.addMqRecord(dto);
        if (StringUtils.isBlank(id)) {
            log.error("保存MQ消费记录失败");
            return;
        }

        List<String> cfgThirdNoticeIdList = cfgThirdNoticeList.stream().map(CfgThirdNoticeEntity::getId).collect(Collectors.toList());

        //三方通知配置--规则条件
        List<CfgRuleConditionEntity> ruleConditionList = cfgRuleConditionService.lambdaQuery().in(CfgRuleConditionEntity::getRuleId, cfgThirdNoticeIdList).list();
        Map<String, List<CfgRuleConditionEntity>> ruleConditionMap = ruleConditionList.stream().collect(Collectors.groupingBy(CfgRuleConditionEntity::getRuleId));

        List<String> isQualifiedCfgIds = new  ArrayList<>();
        for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeList) {
            List<CfgRuleConditionEntity> ruleList = ruleConditionMap.getOrDefault(noticeEntity.getId(), null);
            //校验规则条件
            if (checkRule(dto, noticeEntity, ruleList, businessKey)) {
                isQualifiedCfgIds.add(noticeEntity.getId());
            }
        }

        //获取到符合条件的配置
        if(isQualifiedCfgIds.size() > 0){
            dto.setIsQualifiedCfgIds(isQualifiedCfgIds);
            //设置记录ID并触发通知
            dto.setMqConsumerRecordId(id);
            sendThirdNoticeByMq(dto);
        }
    }

    /**
     * 构建MQ记录DTO
     */
    @Override
    public MqConsumerRecordDTO.MqDTO buildMqRecordDTO(Map<String, Object> jsonMap,
                                                      List<String> diffFields,
                                                      String businessKey) {
        // 关键参数校验
        String db = String.valueOf(jsonMap.getOrDefault("db", ""));
        String table = String.valueOf(jsonMap.getOrDefault("table", ""));
        String operationType = String.valueOf(jsonMap.getOrDefault("P_TAG_IUD", ""));

        if (StringUtils.isAnyBlank(db, table, operationType)) {
            log.error("关键参数缺失 - db:{}, table:{}, operationType:{}", db, table, operationType);
            return null;
        }

        // 字段格式转换
        Map<String, Object> convertedMap = convertToCamelCaseMap(jsonMap);
        if (MapUtils.isEmpty(convertedMap)) {
            log.error("参数转换失败 - convertedMap:{}", jsonMap);
            return null;
        }

        // 构建DTO
        MqConsumerRecordDTO.MqDTO dto = new MqConsumerRecordDTO.MqDTO();
        dto.setDb(db);
        dto.setTable(table);
        dto.setOperationType(operationType);
        dto.setDataJson(convertedMap);
        dto.setBusinessKey(businessKey);
        dto.setDiffFields(diffFields);

        return dto;
    }

    /**
     *  根据table获取单据类型
     */
    @Override
    public String getBusinessKeyWithCache(String table){
        Object obj = redisUtil.hget(TABLE_BUSINESS_KEY, table);
        String bussinessKey = "";
        if(Objects.nonNull(obj)){
            bussinessKey = String.valueOf(obj);
        }else {
            List<CfgQueryOptionEntity> cfgQueryOptionEntityList = FeignQuery.create(CfgQueryOptionEntity.class)
                    .eq(CfgQueryOptionEntity::getTableName, table)
                    .eq(CfgQueryOptionEntity::getFieldBelongsType,CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode()) //限定主表类型
                    .ne(CfgQueryOptionEntity::getBussinessKey, "")
                    .last( "limit 1")
                    .list();
            if(CollUtil.isEmpty(cfgQueryOptionEntityList)){
                return bussinessKey;
            }
            bussinessKey = cfgQueryOptionEntityList.get(0).getBussinessKey();

            //缓存table 和 busineskey的映射关系，有效期1小时
            redisUtil.hset(TABLE_BUSINESS_KEY,table,bussinessKey,3600);
        }
        return bussinessKey;
    }

    /**
     *  下划线转驼峰
     */
    @Override
    public Map<String, Object> convertToCamelCaseMap(Map<String, Object> jsonMap) {
        if (MapUtils.isEmpty(jsonMap)) {
            return Collections.emptyMap();
        }

        Map<String, Object> convertedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String originalKey = entry.getKey();
            Object value = entry.getValue();
            String camelCaseKey = CharSequenceUtil.toCamelCase(originalKey);
            convertedMap.put(camelCaseKey, value);
        }
        return convertedMap;
    }

    /**
     * mq消费发送消息（即时推送）
     * @author jack
     * @date 2025-05-30
     */
    @Override
    public void sendThirdNoticeByMq(MqConsumerRecordDTO.MqDTO dto) {
        Map<String, Object> variablesMap = dto.getDataJson();
        String code = String.valueOf(variablesMap.getOrDefault("code", ""));
        //主键id
        String businessId = String.valueOf(variablesMap.getOrDefault("id", ""));

        //根据参数判断一下通知的单据类型
        CfgQueryOptionDTO.MqParamsDTO mqParamsDTO = new CfgQueryOptionDTO.MqParamsDTO();
        mqParamsDTO.setTableName(dto.getTable());
        String[] split = dto.getDb().split("-");
        String sysClassify = split[split.length - 1];
        mqParamsDTO.setSysClassify(sysClassify);
        //查询出common 、表头、明细的配置
        List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionFeign.listByMqParams(mqParamsDTO);
        if (CollUtil.isEmpty(cfgQueryOptionList)) {
            return;
        }
        //单据类型
        String bussinessKey = dto.getBusinessKey();
        //符合规则的配置主键id集合
        List<String> cfgThirdNoticeIdList = dto.getIsQualifiedCfgIds();
        if (CollUtil.isEmpty(cfgThirdNoticeIdList)) {
            return;
        }

        //获取三方通知配置的信息--单条--即时通知
        List<CfgThirdNoticeEntity> cfgThirdNoticeList = cfgThirdNoticeService.lambdaQuery()
                .eq(CfgThirdNoticeEntity::getBusinessType, bussinessKey)
                .eq(CfgThirdNoticeEntity::getMethod, CfgThirdNoticeMethodEnum.SINGLE.getCode())
                .eq(CfgThirdNoticeEntity::getNoticeStatus, Boolean.TRUE)
                .in(CfgThirdNoticeEntity::getId,cfgThirdNoticeIdList)
                .list();
        if (CollUtil.isEmpty(cfgThirdNoticeList)) {
            return;
        }

        //三方通知配置--推送消息字段配置
        List<CfgApproveSyncFieldMapEntity> fieldMapList = cfgApproveSyncFieldMapService.lambdaQuery().in(CfgApproveSyncFieldMapEntity::getMainId, cfgThirdNoticeIdList).list();
        Map<String, List<CfgApproveSyncFieldMapEntity>> fieldMap = fieldMapList.stream().collect(Collectors.groupingBy(CfgApproveSyncFieldMapEntity::getMainId));

        //三方通知配置--规则条件
        List<CfgRuleConditionEntity> ruleConditionList = cfgRuleConditionService.lambdaQuery().in(CfgRuleConditionEntity::getRuleId, cfgThirdNoticeIdList).list();
        Map<String, List<CfgRuleConditionEntity>> ruleConditionMap = ruleConditionList.stream().collect(Collectors.groupingBy(CfgRuleConditionEntity::getRuleId));

        try {
            for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeList) {
                List<CfgApproveSyncFieldMapEntity> fieldList = fieldMap.getOrDefault(noticeEntity.getId(), null);

                List<CfgRuleConditionEntity> ruleList = ruleConditionMap.getOrDefault(noticeEntity.getId(), null);

                //根据配置发送通知
                sendMsgByCfg(dto, noticeEntity, ruleList, bussinessKey,fieldList , cfgQueryOptionList);
            }
        } catch (Exception e) {
            //保持一条推送失败记录，用于重新推送（全部配置）
            saveFailedRecord(dto, businessId, bussinessKey, code);
            log.error("sendMsg 异常", e);
        }
    }

    private void saveFailedRecord(MqConsumerRecordDTO.MqDTO dto, String businessId, String bussinessKey, String code) {
        ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
        recordEntity.setCfgThirdNoticeId("");
        recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
        recordEntity.setBusinessId(businessId);
        recordEntity.setBusinessType(bussinessKey);
        recordEntity.setBusinessCode(code);
        recordEntity.setNoticeMethod("");
        recordEntity.setReceiverId("");
        recordEntity.setReceiverName("");
        recordEntity.setSendTime(LocalDateTime.now());
        recordEntity.setTitle("");
        recordEntity.setContent("");
        recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
        recordEntity.setErrorReason("sendMsg异常：消费失败");
        Map<String, Object> dataJson = BeanUtil.beanToMap(dto);
        dataJson.put("thirdNoticePushFailedType", ThirdNoticePushFailedTypeEnum.ALL.getCode());
        recordEntity.setDataJson(dataJson);
        insertBatch(Arrays.asList(recordEntity));
    }

    /**
     * 根据第三方消息通知配置进行消息推送 （mq）
     * */
    @Override
    public void sendMsgByCfg(MqConsumerRecordDTO.MqDTO dto, CfgThirdNoticeEntity noticeEntity, List<CfgRuleConditionEntity> ruleList, String bussinessKey, List<CfgApproveSyncFieldMapEntity> fieldList, List<CfgQueryOptionEntity> cfgQueryOptionList) {
        Map<String, Object> variablesMap = dto.getDataJson();
        String code = String.valueOf(variablesMap.getOrDefault("code", ""));
        //主键id
        String businessId = String.valueOf(variablesMap.getOrDefault("id", ""));
        //没有可以发送的字段
        if(CollUtil.isEmpty(fieldList)){
            return;
        }
        //根据通知方式查找人员 目前只有飞书
        if (StringUtils.isNotBlank(noticeEntity.getNoticeMethod())) {
            List<String> noticeMethodList = Arrays.asList(noticeEntity.getNoticeMethod().split(","));
            for (String noticeMethod : noticeMethodList) {
                String roleType = noticeEntity.getRoleType();
                String specificPerson = noticeEntity.getSpecificPerson();
                String post = noticeEntity.getPost();
                if (StringUtils.isBlank(post) && StringUtils.isBlank(roleType) && StringUtils.isBlank(specificPerson)) {
                    String errorReason = "通知配置通知人员不能为空";
                    saveFailedRecordByType(dto, noticeEntity,noticeMethod, businessId, bussinessKey,errorReason, code,ThirdNoticePushFailedTypeEnum.NOPERSON.getCode());
                    continue;
                }

                List<String> userIdList = getUserList(post,roleType, specificPerson, businessId, noticeEntity.getBusinessType());
                if (CollUtil.isEmpty(userIdList)) {
                    //如果没有unionId，则保存失败记录
                    String errorReason = "通知人员不存在";
                    saveFailedRecordByType(dto, noticeEntity,noticeMethod, businessId, bussinessKey,errorReason, code,ThirdNoticePushFailedTypeEnum.NOPERSON.getCode());
                    continue;
                }

                //获取飞书的unionid 与用户关系
                if (CfgApproveSyncSyncPlatformEnum.FEISHU.getCode().equals(noticeMethod)) {
                    List<ThirdUnionDTO> unionList  = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode() , userIdList);

                    Map<String, ThirdUnionDTO> unionMap = unionList.stream().collect(Collectors.toMap(ThirdUnionDTO::getUserId, e -> e, (o1, o2) -> o1));

                    //组装推送消息请求体

                    fieldList.stream().sorted(Comparator.comparing(CfgApproveSyncFieldMapEntity::getSort))
                            .collect(Collectors.toList());

                    //获取需要推送的表字段（包括common、主表、明细）
                    List<String> fieldIds = fieldList.stream().map(CfgApproveSyncFieldMapEntity::getFieldId).collect(Collectors.toList());

                    //过滤出需要推送的表字段，并且根据字段所属单据类型进行分组
                    Map<String, List<CfgQueryOptionEntity>> fieldBelongsTypeByMap = cfgQueryOptionList.stream()
                            .collect(Collectors.groupingBy(CfgQueryOptionEntity::getFieldBelongsType));
                    //获取主表字段配置
                    List<CfgQueryOptionEntity> mainCfgQueryOptionList = fieldBelongsTypeByMap.get(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode());
                    //遍历
                    for (Map.Entry<String, List<CfgQueryOptionEntity>> entry : fieldBelongsTypeByMap.entrySet()) {
                        //common和主表不需要再查询
                        if(entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode()) || entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                            continue;
                        }
                        //判断fieldIds 里是否存在某个明细的字段
                        List<CfgQueryOptionEntity> value = entry.getValue();
                        CfgQueryOptionEntity cfgQueryOptionDetail = value.stream().filter(e -> fieldIds.contains(e.getId())).findFirst().orElse(null);
                        if(Objects.isNull(cfgQueryOptionDetail)){
                            continue;
                        }
                        //如果存在，则需要找对明细表里的关联字段，并根据该字段来进行FeignQuery查询出对应的明细列表
                        CfgQueryOptionEntity refEntity = value.stream().filter(e -> StringUtils.isNotBlank(e.getParentId())).findFirst().orElse(null);
                        if (Objects.isNull(refEntity)) {
                            continue;
                        }

                        //获取关联记录
                        String parentId = refEntity.getParentId();
                        CfgQueryOptionEntity mainEntity = mainCfgQueryOptionList.stream().filter(e -> e.getId().equals(parentId)).findFirst().orElse(null);
                        String mainField = mainEntity.getConditionField();
                        String mainValue = String.valueOf(variablesMap.get(mainField));

                        String classpath = cfgQueryOptionDetail.getClasspath();
                        classpath = classpath.replace("class ", "");
                        Class<BaseEntity> clazz = null;
                        try {
                            clazz = (Class<BaseEntity>) Class.forName(classpath);
                        } catch (ClassNotFoundException e) {
                            String errorReason = "配置实体不存在";
                            saveFailedRecordByType(dto, noticeEntity,noticeMethod, businessId, bussinessKey,errorReason, code,ThirdNoticePushFailedTypeEnum.NOPERSON.getCode());
                            continue;
                        }
                        List<BaseEntity> detailList = FeignQuery.create(clazz)
                                .eq(refEntity.getConditionField(), mainValue)
                                .list();

                        if (CollUtil.isEmpty(detailList)) {
                            continue;
                        }
                        //明细数据
                        variablesMap.put(entry.getKey() , BeanUtil.copyToList(detailList,Map.class));
                    }

                    //进行值映射处理
                    Map<String,String> handlerValueMap = new HashMap<>();
                    Map<String,String> remoteValues = new HashMap<>();
                    for (CfgApproveSyncFieldMapEntity entity : fieldList) {
                        //设置原始值
                        Object fieldValue = variablesMap.getOrDefault(entity.getFieldSource(), "");
                        if(Objects.isNull(fieldValue)){
                            handlerValueMap.put(entity.getFieldId(),"");
                        }else {
                            handlerValueMap.put(entity.getFieldId(),String.valueOf(fieldValue));
                        }
                        //获取CfgApproveSyncFieldMap对应该的配置记录
                        CfgQueryOptionEntity queryOptionEntity = cfgQueryOptionList.stream().filter(e -> Objects.equals(entity.getFieldId(), e.getId())).findFirst().orElse(null);
                        if(Objects.isNull(queryOptionEntity)){
                            continue;
                        }
                        //判断是类型是common、主表还是明细
                        if(queryOptionEntity.getFieldBelongsType().equals(CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode())
                                || queryOptionEntity.getFieldBelongsType().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                            String fieldSourceValueStr = getFieldSourceValueStr(entity.getFieldSource(), variablesMap);
                            if(StringUtils.isNotBlank(fieldSourceValueStr)){
                                handlerValueMap.put(entity.getFieldId(),fieldSourceValueStr);
                            }
                        }else{//其余均为明细表
                            List<Object> detail =( List<Object> ) variablesMap.get(queryOptionEntity.getFieldBelongsType());
                            if(CollUtil.isNotEmpty(detail)){
                                StringBuffer sb = new StringBuffer();
                                for (Object object : detail) {
                                    Map<String, Object> map = BeanUtil.beanToMap(object);
                                    String string = getFieldSourceValueStr(entity.getFieldSource(), map);
                                    if(StringUtils.isNotBlank(string)){
                                        sb.append(string);
                                        sb.append(",");
                                    }
                                }
                                String fieldSourceDetailValueStr = sb.toString();
                                if(StringUtils.isNotBlank(fieldSourceDetailValueStr)){
                                    if (fieldSourceDetailValueStr.endsWith(",")) {
                                        fieldSourceDetailValueStr = fieldSourceDetailValueStr.substring(0, fieldSourceDetailValueStr.length() - 1); // 移除最后一个逗号
                                    }
                                    handlerValueMap.put(entity.getFieldId(),fieldSourceDetailValueStr);
                                }
                            }
                        }
                    }
                    //需要进行值映射
                    if(handlerValueMap.size() > 0) {
                        remoteValues = cfgQueryOptionFeign.getRemoteValues(handlerValueMap);
                    }


                    //通知标题
                    String title = noticeEntity.getTitle();
                    //通知主题
                    StringBuffer sb = new StringBuffer();
                    String noticeType = noticeEntity.getNoticeType();
                    sb.append("通知类型：");
                    sb.append(noticeType);
                    sb.append("\n");
                    for (CfgApproveSyncFieldMapEntity entity : fieldList) {
                        sb.append(entity.getFieldName());
                        sb.append("：");
                        sb.append(remoteValues.getOrDefault(entity.getFieldId(),""));
                        sb.append("\n");
                    }
                    String content = sb.toString();
                    //跳转URL
                    String url = noticeEntity.getUrl();
                    Map<String, Object> contentMap = fsService.getCardMessageMap(title, content, url);

                    for (String userId : userIdList) {
                        ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
                        recordEntity.setCfgThirdNoticeId(noticeEntity.getId());
                        recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
                        recordEntity.setBusinessId(businessId);
                        recordEntity.setBusinessType(bussinessKey);
                        recordEntity.setBusinessCode(code);
                        recordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        recordEntity.setReceiverId(userId);
                        recordEntity.setReceiverName("");
                        recordEntity.setSendTime(LocalDateTime.now());
                        recordEntity.setTitle(noticeEntity.getTitle());
                        recordEntity.setContent(content);
                        recordEntity.setErrorReason("");
                        recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.SENDING.getCode());
                        contentMap.put("thirdNoticePushFailedType", ThirdNoticePushFailedTypeEnum.SENDNOTICE.getCode());
                        contentMap.put("cfgThirdNoticeId", noticeEntity.getId());
                        recordEntity.setDataJson(contentMap);
                        if(!unionMap.containsKey(userId) || StringUtils.isBlank(unionMap.get(userId).getThirdUnionId())){
                            recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
                            recordEntity.setErrorReason(ApiError.FS_USER_NOT_BIND.msg);
                            insertBatch(Arrays.asList(recordEntity));
                            continue;
                        }

                        String thirdUnionId = unionMap.get(userId).getThirdUnionId();
                        Boolean save = insertBatch(Arrays.asList(recordEntity));
                        if(Boolean.TRUE.equals(save)){
                            SendThirdNoticeConsumerDTO sendMessage = new SendThirdNoticeConsumerDTO();
                            sendMessage.setMessageId(recordEntity.getId());
                            sendMessage.setUnionIds(Arrays.asList(thirdUnionId));
                            sendMessage.setContentMap(contentMap);
                            SendResult sendResult = mqProducerService.syncClassMsg(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC, RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.getName(), sendMessage, IdUtil.simpleUUID());
                            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                                throw new ServiceException(StrUtil.format("MQ数据异常，{}", JSONUtil.toJsonStr(sendResult)));
                            }
                            log.info("MQ数据结果：{}", JSONUtil.toJsonStr(sendResult));
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存推送失败的记录
     * */
    @Override
    public void saveFailedRecordByType(MqConsumerRecordDTO.MqDTO dto, CfgThirdNoticeEntity noticeEntity, String noticeMethod, String businessId, String bussinessKey, String errorReason, String code, String thirdNoticePushFailedType) {
        ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
        recordEntity.setCfgThirdNoticeId(noticeEntity.getId());
        recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
        recordEntity.setBusinessId(businessId);
        recordEntity.setBusinessType(bussinessKey);
        recordEntity.setBusinessCode(code);
        recordEntity.setReceiverId("");
        recordEntity.setReceiverName("");
        recordEntity.setNoticeMethod(noticeMethod);
        recordEntity.setSendTime(LocalDateTime.now());
        recordEntity.setTitle(noticeEntity.getTitle());
        recordEntity.setContent("");
        recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
        recordEntity.setErrorReason(errorReason);
        Map<String, Object> dataJson = BeanUtil.beanToMap(dto);
        dataJson.put("thirdNoticePushFailedType", thirdNoticePushFailedType);
        dataJson.put("cfgThirdNoticeId",noticeEntity.getId());
        recordEntity.setDataJson(dataJson);
        insertBatch(Arrays.asList(recordEntity));
    }

    @Override
    public boolean checkRule(MqConsumerRecordDTO.MqDTO dto, CfgThirdNoticeEntity noticeEntity, List<CfgRuleConditionEntity> cfgRuleConditionEntities, String bussinessKey) {
        if (CollUtil.isNotEmpty(cfgRuleConditionEntities)) {
            //获取变动字段
            List<String> diffFields = dto.getDiffFields();
            //变动字段中不存在规则条件中
            List<String> fieldList = cfgRuleConditionEntities.stream().map(CfgRuleConditionEntity::getField).collect(Collectors.toList());
            //如果只有一个通知节点做规则条件
            if (fieldList.size() == 1 && fieldList.get(0).equals(CfgQueryOptionExtendTypeEnum.NOTICENODE.getCode())) {

            }else {
                //如果变动字段中不存在规则条件中的字段，则返回false
                boolean existField = fieldList.stream().filter(e -> !Objects.equals(e, CfgQueryOptionExtendTypeEnum.NOTICENODE.getCode())).anyMatch(diffFields::contains);
                if(!existField){
                    return Boolean.FALSE;
                }
            }

            //规则条件转map
            Map<String, String> cfgRuleConditionMap = cfgRuleConditionEntities.stream().collect(Collectors.toMap(CfgRuleConditionEntity::getField, CfgRuleConditionEntity::getValue,(o1,o2) -> o2));

            Map<String, Object> variablesMap = dto.getDataJson();
            //主键id
            String businessId = String.valueOf(variablesMap.getOrDefault("id", ""));
            //单据类型
            String businessType = noticeEntity.getBusinessType();

            //查询是否有拓展
            CfgQueryOptionDTO.ListByFieldDTO listByFieldDTO = new CfgQueryOptionDTO.ListByFieldDTO();
            listByFieldDTO.setBusinessType(businessType);
            listByFieldDTO.setFieldList(fieldList);
            List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionFeign.listExtendByFieldCondition(listByFieldDTO);
            List<String> cfgQueryOptionfieldList = new ArrayList<>();
            if(CollUtil.isNotEmpty(cfgQueryOptionList)){
                for (CfgQueryOptionEntity cfgQueryOptionEntity : cfgQueryOptionList) {
                    cfgQueryOptionfieldList.add(cfgQueryOptionEntity.getConditionField());
                    //规则条件字段对应的值
                    String feildValue = cfgRuleConditionMap.get(cfgQueryOptionEntity.getConditionField());
                    //提审
                    if(ThirdNoticePushRecordNoticeNodeEnum.WAIT_SUBMITTO_APPROVEING.getCode().equals(feildValue)){
                        //提交操作
                        ProcessManagementDTO.CheckSubmitByBusinessIdDTO checkSubmitByBusinessIdDTO = new ProcessManagementDTO.CheckSubmitByBusinessIdDTO();
                        checkSubmitByBusinessIdDTO.setBusinessId(businessId);
                        checkSubmitByBusinessIdDTO.setBusinessKey(bussinessKey);
                        Boolean allMatch = workflowFeign.checkSubmitByBusinessId(checkSubmitByBusinessIdDTO);
                        if(Boolean.FALSE.equals(allMatch)){
                            return Boolean.FALSE;
                        }
                        variablesMap.put(cfgQueryOptionEntity.getConditionField(), feildValue);
                    }
                    //新增
                    else if(ThirdNoticePushRecordNoticeNodeEnum.ADD_RECORD.getCode().equals(feildValue)){
                        //不等于新增则返回false
                        if(!Objects.equals(ThirdNoticeRecordOperationTypeEnum.INSERT.getCode(), dto.getOperationType())){
                            return Boolean.FALSE;
                        }
                        variablesMap.put(cfgQueryOptionEntity.getConditionField(), feildValue);
                    }
                    //新品 / 老品
                    else if(ThirdNoticePushRecordNoticeNodeEnum.NOT_SUBSEQUENT_BATCH.getCode().equals(feildValue) || ThirdNoticePushRecordNoticeNodeEnum.SUBSEQUENT_BATCH.getCode().equals(feildValue)){
                        List<QcResultDTO.QcNoticeDTO> list = wmsTaskFeign.listQcResultMsg(Arrays.asList(businessId));
                        //判空
                        if(CollUtil.isEmpty(list)){
                            return Boolean.FALSE;
                        }
                        //以新 老品分组
                        Map<Boolean, List<QcResultDTO.QcNoticeDTO>> map = list.stream().collect(Collectors.groupingBy(v -> !FirstMassProductTypeEnum.SUBSEQUENT_BATCH.getCode().equals(v.getFirstMassProduct())));
                        // 根据条件选择对应的新品或老品数据
                        list = ThirdNoticePushRecordNoticeNodeEnum.NOT_SUBSEQUENT_BATCH.getCode().equals(feildValue)
                                ? map.get(Boolean.TRUE)
                                : map.get(Boolean.FALSE);
                        if (CollUtil.isEmpty(list)) {
                            return Boolean.FALSE;
                        }
                        variablesMap.put(cfgQueryOptionEntity.getConditionField(), feildValue);
                    }
                    //产品尺寸变更
                    else if(ThirdNoticePushRecordNoticeNodeEnum.QC_BACK_FILL_PACKAGING.getCode().equals(feildValue)){
                        variablesMap.put(cfgQueryOptionEntity.getConditionField(), feildValue);
                    }
                    else {
                        //表字段值变化
                        if(diffFields.contains(feildValue)){
                            variablesMap.put(cfgQueryOptionEntity.getConditionField(), feildValue);
                        }else {
                            return Boolean.FALSE;
                        }
                    }
                }
            }else {
                return Boolean.FALSE;
            }

            //封装条件参数
            Map<String, Object> map = cfgRuleConditionEntities.stream()
                    .collect(Collectors.toMap(
                            CfgRuleConditionEntity::getField,
                            e -> variablesMap.get(e.getField())
                    ));

            if(CollUtil.isNotEmpty(map)){
                // 获取所有符合条件的规则
                List<CfgRuleConditionEntity> conditionList = cfgRuleConditionEntities.stream()
                        .sorted(Comparator.comparing(CfgRuleConditionEntity::getIndex))
                        .collect(Collectors.toList());
                List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
                //获取到表达式,判断表达式是否匹配
                Boolean match = spElServer.matchExpressionDefaultByConditionList(conditionElementList, map,"");
                if(Boolean.FALSE.equals(match)){
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }


    /**
     * 获取系统设置的岗位人员(去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    @Override
    public List<String> getUserList(String post, String roleType, String specificPerson, String businessId, String businessKey) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(post)){
            List<String> postIdList = Arrays.asList(post.split(","));

            //岗位id
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                resultList.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }
        //
        if(StringUtils.isNotBlank(roleType)){
            List<String> roleId = Arrays.asList(roleType.split(","));
            List<DictNoticeRoleOptionEntity> list = dictNoticeRoleOptionService.lambdaQuery().in(DictNoticeRoleOptionEntity::getId, roleId).list();
            if(CollUtil.isNotEmpty(list)){
                for (DictNoticeRoleOptionEntity optionEntity : list) {
                    String field = optionEntity.getField();
                    String classPath = optionEntity.getClassPath();
                    String refField = optionEntity.getRefField();
                    if(StringUtils.isNotBlank(field) && StringUtils.isNotBlank(classPath)) {
                        try {
                            if (classPath.contains("getCurApprover")) {
                                //获取当前审批人逻辑需要特殊处理
                                String[] split = classPath.split("#");
                                String controller = split[0];
                                String methodName = split[1];
                                ProcessManagementDTO.HistoryActivityDTO dto = new ProcessManagementDTO.HistoryActivityDTO();
                                dto.setBusinessKey(businessKey);
                                dto.setBusinessId(businessId);
                                ApiResult select = FeignQuery.invoke(ApiResult.class, controller, methodName, Arrays.asList(dto));
                                if(Objects.nonNull(select)){
                                    List<ProcessManagementDTO.CurApproveInfoDTO> data = JSON.parseArray(JSON.toJSONString(select.getData()), ProcessManagementDTO.CurApproveInfoDTO.class);
                                    if(CollUtil.isNotEmpty(data)){
                                        resultList.addAll(Arrays.asList(data.get(0).getCurApproveId().split(",")));
                                    }
                                }
                            }else if (classPath.contains("listQcItemRolePeople")) {
                                //质检单角色人员需要取sku的项目经理和产品经理
                                String[] split = classPath.split("#");
                                String controller = split[0];
                                String methodName = split[1];
                                //参数 是否首批
                                Boolean isFirstMassProduct =  Boolean.parseBoolean(split[2]);
                                //根据质检单id去查询sku中的项目经理和产品经理
                                QcResultDTO.QcItemRolePeopleDTO dto = new QcResultDTO.QcItemRolePeopleDTO();
                                dto.setQcInfoIds(Arrays.asList(businessId));
                                dto.setIsFirstMassProduct(isFirstMassProduct);
                                ApiResult select = FeignQuery.invoke(ApiResult.class, controller, methodName, Arrays.asList(dto));
                                if(Objects.nonNull(select)){
                                    // 转换为 Map
                                    Map<String, String> data = JSON.parseObject(JSON.toJSONString(select.getData()), Map.class);
                                    if(CollUtil.isNotEmpty(data)){
                                        resultList.addAll(Arrays.asList(data.get(field).split(",")));
                                    }
                                }
                            } else {
                                //根据配置查询
                                String ref = "id";
                                Class<BaseEntity> clazz = (Class<BaseEntity>) Class.forName(classPath);
                                if (StringUtils.isNotBlank(refField) && optionEntity.getTableType().equals(DictNoticeRoleOptionTableTypeEnum.DETAIL.getCode())) {
                                    ref = refField;
                                }
                                List<BaseEntity> baseEntityList = FeignQuery.create(clazz)
                                        .eq(ref, businessId)
                                        .list();
                                if (CollUtil.isNotEmpty(baseEntityList)) {
                                    // 获取字段值
                                    String result = baseEntityList.stream()
                                            .map(item -> String.valueOf(ReflectUtil.getFieldValue(item, field)))
                                            .filter(value -> StringUtils.isNotBlank(value))
                                            .collect(Collectors.joining(","));
                                    if (StringUtils.isNotBlank(result)) {
                                        resultList.add(result);
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            log.error("执行 getUserList 失败", e);
                        }
                    }
                }
            }
        }
        return resultList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
    }

    public String getFieldSourceValueStr(String fieldSource, Map<String, Object> variablesMap) {
        Object fieldSourceValue = variablesMap.getOrDefault(fieldSource,null);
        if(Objects.nonNull(fieldSourceValue)){
            return getFieldSourceValueStr(fieldSourceValue);
        }
        return "";
    }

    public  String getFieldSourceValueStr(Object fieldSourceValue) {
        String fieldSourceValueStr = "";
        if (fieldSourceValue != null) {
            if (fieldSourceValue instanceof String) {
                fieldSourceValueStr = (String) fieldSourceValue;
            } else if (fieldSourceValue instanceof Integer) {
                fieldSourceValueStr = String.valueOf(fieldSourceValue);
            } else if (fieldSourceValue instanceof Date) {
                // 假设日期格式为 yyyy-MM-dd HH:mm:ss
                fieldSourceValueStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) fieldSourceValue);
            } else if (fieldSourceValue instanceof java.time.LocalDate) {
                fieldSourceValueStr = ((java.time.LocalDate) fieldSourceValue).toString();
            } else if (fieldSourceValue instanceof java.time.LocalDateTime) {
                fieldSourceValueStr = ((java.time.LocalDateTime) fieldSourceValue).toString();
            } else if (fieldSourceValue instanceof Boolean) {
                fieldSourceValueStr = String.valueOf(fieldSourceValue);
            } else if (fieldSourceValue instanceof Double || fieldSourceValue instanceof Float || fieldSourceValue instanceof Long) {
                fieldSourceValueStr = String.valueOf(fieldSourceValue);
            } else {
                // 其他类型，尝试直接调用 toString()
                fieldSourceValueStr = fieldSourceValue.toString();
            }
        }
        return fieldSourceValueStr;
    }


    /**
     * 获取第三方通知推送记录 （避免重复推送）
     * @author jack
     * @date 2025-05-30
     */
    @Override
    public List<ThirdNoticePushRecordEntity> listSendingRecord(ThirdNoticePushRecordDTO.ParamsDTO paramsDTO) {
        if(Objects.isNull(paramsDTO)){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ThirdNoticePushRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        //ThirdNoticePushRecordDTO.ParamsDTO paramsDTO里的所有参数如果不为空，则添加到queryWrapper中
        if(StringUtils.isNotBlank(paramsDTO.getBusinessType())){
            queryWrapper.eq(ThirdNoticePushRecordEntity::getBusinessType, paramsDTO.getBusinessType());
        }
        if(StringUtils.isNotBlank(paramsDTO.getNoticeType())){
            queryWrapper.eq(ThirdNoticePushRecordEntity::getNoticeType, paramsDTO.getNoticeType());
        }
        if(StringUtils.isNotBlank(paramsDTO.getNoticeMethod())){
            queryWrapper.eq(ThirdNoticePushRecordEntity::getNoticeMethod, paramsDTO.getNoticeMethod());
        }
        if(StringUtils.isNotBlank(paramsDTO.getStatus())){
            queryWrapper.eq(ThirdNoticePushRecordEntity::getStatus, paramsDTO.getStatus());
        }
        if(CollUtil.isNotEmpty(paramsDTO.getUserIds())){
            queryWrapper.in(ThirdNoticePushRecordEntity::getReceiverId, paramsDTO.getUserIds());
        }
        if (paramsDTO.getSendTime() != null) {
            // 获取起始时间和结束时间
            LocalDateTime startOfDay = paramsDTO.getSendTime().with(LocalTime.MIN);
            LocalDateTime endOfDay = paramsDTO.getSendTime().with(LocalTime.MAX);
            queryWrapper.between(ThirdNoticePushRecordEntity::getSendTime, startOfDay, endOfDay);
        }
        return this.list(queryWrapper);
    }


    /**
     * 定时任务:发送第三方通知（汇总类型）
     * @author jack
     * @date 2025-05-30
     */
    @Override
    public void sendThirdNoticeJob() {
        List<CfgThirdNoticeEntity> cfgThirdNoticeEntities = cfgThirdNoticeService.listByMethod(CfgThirdNoticeMethodEnum.SUMMARY.getCode());
        if(CollUtil.isNotEmpty(cfgThirdNoticeEntities)){
            LocalDateTime now = LocalDateTime.now();
            for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeEntities) {
                String roleType = noticeEntity.getRoleType();
                String specificPerson = noticeEntity.getSpecificPerson();
                String post = noticeEntity.getPost();
                if (StringUtils.isBlank(post) && StringUtils.isBlank(roleType) && StringUtils.isBlank(specificPerson)) {
                    continue;
                }
                //目前只支持几种单据
                String businessType = noticeEntity.getBusinessType();
                if(SourceTypeEnum.QC_INFO.getCode().equals(businessType)
                        || SourceTypeEnum.LOGISTICS_BILL.getCode().equals(businessType)
                        || SourceTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode().equals(businessType)
                        ||SourceTypeEnum.PRODUCT_REGISTRATION.getCode().equals(businessType)){
                }else {
                    continue;
                }
                //延迟等级
                int delayLevel = -1;
                String cron = noticeEntity.getCron();
                // 校验cron表达式
                boolean isValid = CronExpression.isValidExpression(cron);
                if(Boolean.FALSE.equals(isValid)){
                    //cron表达式不合法 记录错误日志
                    log.error("cron表达式不合法，Cron：{}", cron);
                    continue;
                }else {
                    //解析cron表达式，获取下次执行时间
                    try {
                        CronExpression cronExpression = new CronExpression(cron);
                        LocalDateTime nextExecutionTime = cronExpression.getNextValidTimeAfter(new Date()).toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                        log.info("Cron表达式解析成功，下次执行时间为：{}", nextExecutionTime);

                        // 根据下次执行时间，发送延迟消息到RocketMQ
                        if (nextExecutionTime == null) continue;
                        Duration delay = Duration.between(now, nextExecutionTime);
                        delayLevel = mapDelayLevel(delay);
                        if (delayLevel == -1) continue;
                    } catch (Exception e) {
                        log.error("解析Cron表达式失败，Cron：{}", cron);
                        continue;
                    }
                }

                //通知标题
                String title = noticeEntity.getTitle();
                //通知主题
                StringBuffer sb = new StringBuffer();
                String noticeType = noticeEntity.getNoticeType();
                sb.append("通知类型：");
                sb.append(noticeType);
                sb.append("\n");
                String content = sb.toString();
                if (SourceTypeEnum.QC_INFO.getCode().equals(businessType)) {
                    //质检单--质检通知
                    sendFsQcNotice(noticeEntity,post, roleType, specificPerson, title, now, content, delayLevel);
                } else if (SourceTypeEnum.LOGISTICS_BILL.getCode().equals(businessType)) {
                    //物流单--在途异常
                    sendFmLogisticWarn(noticeEntity,post, roleType, specificPerson, title, now, content, delayLevel);
                } else if (SourceTypeEnum.PRODUCT_REGISTRATION.getCode().equals(businessType)) {
                    //备案管理
                    sendWhenNotRegistration(noticeEntity,post, roleType, specificPerson, title, content, now, delayLevel);
                } else if (SourceTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode().equals(businessType)){
                    //仓位补货
                    sendWarehouseLocationReplenish(noticeEntity,post, roleType, specificPerson, title, content, now, delayLevel);
                }
            }
        }
    }

    /**
     * 质检单--质检通知
     * @author jack
     * @date 2025-05-30
     */
    private void sendFsQcNotice(CfgThirdNoticeEntity noticeEntity, String post,String roleType, String specificPerson, String title, LocalDateTime now, String content, int delayLevel) {
        List<String> userIdList = getUserList(post, specificPerson);
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }
        title = wmsTaskFeign.getFsQcNoticeTitle(title);
        content = content + "质检通知时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        forSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);

    }
    /**
     * 仓位补货
     * @author jack
     * @date 2025-05-30
     */
    private void sendWarehouseLocationReplenish(CfgThirdNoticeEntity noticeEntity, String post, String roleType, String specificPerson, String title, String content, LocalDateTime now, int delayLevel) {
        List<String> userIdList = getUserList(post, specificPerson);
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }

        //统计仓位补货的各个类型的数量
        String waitHandle = "0";
        String handleIng = "0";
        List<WarehouseLocationReplenishDTO.TabDTO> tabList = wmsTaskFeign.listTabInfo();
        if(CollUtil.isNotEmpty(tabList)){
            for (WarehouseLocationReplenishDTO.TabDTO tab : tabList) {
                String tabFlag = tab.getTabFlag();
                if(tabFlag.equals(ReplenishBillStatusEnum.WAIT_HANDLE.getCode())){
                    waitHandle = tab.getCount()+"";
                }
                if(tabFlag.equals(ReplenishBillStatusEnum.HANDLE_ING.getCode())){
                    handleIng = tab.getCount()+"";
                }
            }
        }
        String msgContent = String.format(NoticeMsgConstant.FS_WLR_SETTING_CONTENT,waitHandle,handleIng, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        content = content + msgContent;
        forSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);
    }
    /**
     * 备案管理
     * @author jack
     * @date 2025-05-30
     */
    private void sendWhenNotRegistration(CfgThirdNoticeEntity noticeEntity, String post, String roleType, String specificPerson, String title, String content, LocalDateTime now, int delayLevel) {
        List<ProductRegistrationEntity> registrationEntities = tmsProductRegistrationFeign.listByRegistered();
        List<String> userIdList = getUserList(post, specificPerson);
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }
        Map<String,List<ProductRegistrationEntity>> map = registrationEntities.stream().collect(Collectors.groupingBy(ProductRegistrationEntity::getDeclareSupplierName));
        for (Map.Entry<String, List<ProductRegistrationEntity>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<ProductRegistrationEntity> value = entry.getValue();
            title = CharSequenceUtil.format(title, key,value.size());
            List<String> skuNoList = value.stream().map(v->v.getSkuNo()).collect(Collectors.toList());
            int size = skuNoList.size();
            if(size<=10){
                content = content + "备案SKU："+ skuNoList;
            }else{
                skuNoList = skuNoList.subList(0,10);
                content = content + "备案SKU："+ skuNoList + "...+"+(size-10);
            }
            forSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);
        }
    }
    /**
     * 物流单--在途异常
     * @author jack
     * @date 2025-05-30
     */
    private void sendFmLogisticWarn(CfgThirdNoticeEntity noticeEntity, String post, String roleType, String specificPerson, String title, LocalDateTime now, String content, int delayLevel) {
        List<TmsFirstMileLogisticDTO.PagingVO> pagingVOS = tmsFirstMileLogisticFeign.hasWarnPaging(new TmsFirstMileLogisticDTO.PagingParamDTO());
        pagingVOS = pagingVOS.stream().filter(v-> Objects.nonNull(v.getWarnHour()) && v.getWarnHour() < 0 && !FmLogisticTrackStatusEnum.SIGN.getCode().equals(v.getLogisticsStatus())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(pagingVOS)){
            List<String> userIdList = getUserList(post, specificPerson);
            if (CollUtil.isEmpty(userIdList)) {
                return;
            }
            //今日超期
            List<TmsFirstMileLogisticDTO.PagingVO> todayPagingVOS = pagingVOS.stream().filter(v-> v.getWarnHour() > -24).collect(Collectors.toList());
            //预警发送人员分为两部分，一部分是销售店铺负责人，一部分是抄送人
            //处理抄送人消息发送
            int totalWarnCount = pagingVOS.size();
            int todayCount = todayPagingVOS.size();
            title = CharSequenceUtil.format(title, totalWarnCount,todayCount);
            forSendByNoticeMethod(noticeEntity, userIdList, now, title, content, delayLevel);

            //处理店铺负责人消息推送
            if(StringUtil.isNotBlank(roleType) && roleType.equals("shopCharge")){
                List<String> shopIdList = pagingVOS.stream().map(TmsFirstMileLogisticDTO.PagingVO::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(shopIdList)){
                    return;
                }
                //封装负责人id
                List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
                for (TmsFirstMileLogisticDTO.PagingVO pagingVO : pagingVOS) {
                    ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingVO.getShopId())).findFirst().orElse(null);
                    if(Objects.nonNull(shopInfoEntity) && StringUtils.isNotBlank(shopInfoEntity.getChargeId())){
                        pagingVO.setChargeId(shopInfoEntity.getChargeId());
                    }
                }
                pagingVOS = pagingVOS.stream().filter(v->StringUtils.isNotBlank(v.getChargeId())).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(pagingVOS)){
                    return;
                }
                Map<String,List<TmsFirstMileLogisticDTO.PagingVO>> pagingMap = pagingVOS.stream().collect(Collectors.groupingBy(TmsFirstMileLogisticDTO.PagingVO::getChargeId));

                for (Map.Entry<String, List<TmsFirstMileLogisticDTO.PagingVO>> entry : pagingMap.entrySet()) {
                    String chargeId = entry.getKey();
                    List<TmsFirstMileLogisticDTO.PagingVO> value = entry.getValue();
                    List<TmsFirstMileLogisticDTO.PagingVO> todayWarnByCharge = value.stream().filter(v-> v.getWarnHour() > -24).collect(Collectors.toList());
                    int totalWarnCountByCharge = value.size();
                    int todayCountByCharge = todayWarnByCharge.size();
                    title = CharSequenceUtil.format(title, totalWarnCountByCharge,todayCountByCharge);
                    forSendByNoticeMethod(noticeEntity, Collections.singletonList(chargeId), now, title, content, delayLevel);
                }
            }
        }
    }
    /**
     * 遍历推送
     * @author jack
     * @date 2025-05-30
     */
    private void forSendByNoticeMethod(CfgThirdNoticeEntity noticeEntity, List<String> userIdList, LocalDateTime now, String title, String content, int delayLevel) {
        //根据通知方式查找人员 目前只有飞书
        String noticeMethod = noticeEntity.getNoticeMethod();
        if (StringUtils.isNotBlank(noticeMethod)) {
            List<String> noticeMethodList = Arrays.asList(noticeMethod.split(","));
            for (String str : noticeMethodList) {
                //获取飞书的unionid 与用户关系
                if (CfgApproveSyncSyncPlatformEnum.FEISHU.getCode().equals(str)) {
                    List<ThirdUnionDTO> unionList = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode() , userIdList);
                    Map<String, ThirdUnionDTO> unionMap = unionList.stream().collect(Collectors.toMap(ThirdUnionDTO::getUserId, e -> e));

                    //根据用户id + businessType + noticeMethod + noticeType 判断是否已经在发送中。
                    ThirdNoticePushRecordDTO.ParamsDTO paramsDTO = new ThirdNoticePushRecordDTO.ParamsDTO();
                    paramsDTO.setBusinessType(noticeEntity.getBusinessType());
                    paramsDTO.setNoticeMethod(noticeMethod);
                    paramsDTO.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
                    paramsDTO.setStatus(ThirdNoticePushRecordStatusEnum.SENDING.getCode());
                    paramsDTO.setUserIds(userIdList);
                    paramsDTO.setSendTime(now);//只查询当天日期的
                    List<ThirdNoticePushRecordEntity> listSendingRecord = listSendingRecord(paramsDTO);
                    Map<String, ThirdNoticePushRecordEntity> sendingMap = listSendingRecord.stream().collect(Collectors.toMap(ThirdNoticePushRecordEntity::getReceiverId, e -> e, (o1, o2) -> o1));

                    for (String userId : userIdList) {
                        //上一次的发送中，则跳过
                        if(sendingMap.containsKey(userId)){
                            continue;
                        }
                        ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
                        recordEntity.setCfgThirdNoticeId(noticeEntity.getId());
                        recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
                        recordEntity.setBusinessType(noticeEntity.getBusinessType());
                        recordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        recordEntity.setReceiverId(userId);
                        if(unionMap.containsKey(userId) &&  StringUtils.isNotBlank(unionMap.get(userId).getUserName())){
                            recordEntity.setReceiverName(unionMap.get(userId).getUserName());
                        }
                        recordEntity.setSendTime(now);
                        recordEntity.setTitle(title);
                        recordEntity.setContent(content);
                        recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.SENDING.getCode());
                        if(!(unionMap.containsKey(userId) &&  StringUtils.isNotBlank(unionMap.get(userId).getThirdUnionId()))){
                            recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
                            recordEntity.setErrorReason(ApiError.FS_USER_NOT_BIND.msg);
                        }
                        boolean save = save(recordEntity);
                        if(Boolean.TRUE.equals(save)){
                            if(unionMap.containsKey(userId) &&  StringUtils.isNotBlank(unionMap.get(userId).getThirdUnionId())){
                                String messageId = recordEntity.getId();
                                SendThirdNoticeConsumerDTO sendMessage = new SendThirdNoticeConsumerDTO();
                                sendMessage.setUnionIds(Collections.singletonList(unionMap.get(userId).getThirdUnionId()));

                                //跳转URL
                                String url = noticeEntity.getUrl();
                                Map<String, Object> contentMap = fsService.getCardMessageMap(title, content, url);
                                sendMessage.setContentMap(contentMap);
                                sendMessage.setMessageId(messageId);
                                SendResult sendResult = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC, RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.getName(), sendMessage, IdUtil.simpleUUID(), delayLevel);
                                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                                    throw new ServiceException(StrUtil.format("MQ数据异常，{}", JSONUtil.toJsonStr(sendResult)));
                                }
                                log.info("MQ数据结果：{}", JSONUtil.toJsonStr(sendResult));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 延迟等级映射
     * @author jack
     * @date 2025-05-30
     */
    public static int mapDelayLevel(Duration delay) {
        long seconds = delay.getSeconds();
        if (seconds <= 1) return 1;
        if (seconds <= 5) return 2;
        if (seconds <= 10) return 3;
        if (seconds <= 30) return 4;
        if (seconds <= 60) return 5;
        if (seconds <= 120) return 6;
        if (seconds <= 180) return 7;
        if (seconds <= 240) return 8;
        if (seconds <= 300) return 9;
        if (seconds <= 360) return 10;
        if (seconds <= 600) return 11;
        if (seconds <= 1200) return 12;
        return -1; // 超出最大延迟等级，忽略
    }


    /**
     * 获取系统设置的人员(去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getUserList(String post, String specificPerson) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(post)){
            List<String> postIdList = Arrays.asList(post.split(","));

            //岗位id
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                resultList.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }
        return resultList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
    }


}
