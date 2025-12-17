package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.*;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * B2C订单标记发货重试
 */
@Component
@Slf4j
public class SoB2cRetryJob {

    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private SoB2cAbnormalService soB2cAbnormalService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private RuleLogisticsService ruleLogisticsService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private OperateLogService operateLogService;

    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String namespace;


    /**
     * B2C订单异常重试任务
     *
     * @Author Jim
     **/
    @XxlJob("SoB2cRetryJob")
    public ReturnT<String> soB2cRetryJob() {
        String redisKey =  CharSequenceUtil.format(RedisKeyConstant.SOB2C_RETRY_JOB, namespace);
        Boolean setSignResult = redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 600, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(setSignResult)) {
            XxlJobHelper.log("SoB2cRetryJob 执行中,当前跳过");
            return ReturnT.FAIL;
        }
        XxlJobHelper.log("SoB2cRetryJob 执行开始");
        try {
            String jobParam = XxlJobHelper.getJobParam();
            int count = 3;
            int intervalHour = 0;
            List<String> typeList = new ArrayList<>();
            typeList.add(SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode());
            Integer maxRetryCount = 3;
            List<String> messageList = new ArrayList<>();
            if (StringUtils.isNotBlank(jobParam)) {
                JSONObject jsonObject = new JSONObject(jobParam);
                count = jsonObject.getInt("count", 3);
                JSONArray jsonArray = jsonObject.getJSONArray("messageList");
                if (CollectionUtils.isNotEmpty(jsonArray)) {
                    messageList = jsonArray.stream().map(Object::toString).collect(Collectors.toList());
                }
                if(jsonObject.containsKey("type")){
                    String typeArr = jsonObject.getStr("type", SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode());
                    typeList = Arrays.asList(typeArr.split(","));
                }
                maxRetryCount = jsonObject.getInt("maxRetryCount", 3);
                intervalHour = jsonObject.getInt("intervalHour", 0);
            }

            LocalDateTime todayNoon = LocalDateTime.now();
            if (intervalHour > 0) {
                todayNoon = LocalDateTime.now().minusHours(intervalHour);
            }
            //获取符合规则的渠道列表
            List<RuleLogisticsEntity> ruleLogisticsEntityList = ruleLogisticsService.getChannelListByAutoSubmitDelivery();
            List<String> channelIdList = ruleLogisticsEntityList.stream().map(RuleLogisticsEntity::getLogisticsChannelId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            for (String type : typeList) {
                LambdaQueryWrapper<SoB2cErrorEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SoB2cErrorEntity::getType, type)
                        .ne(SoB2cErrorEntity::getMainId, "")
                        .le(SoB2cErrorEntity::getUpdateTime, todayNoon)
                        .le(SoB2cErrorEntity::getRetryCount, maxRetryCount);

                if (CollectionUtils.isNotEmpty(messageList)) {
                    for (String keyword : messageList) {
                        queryWrapper.or().like(SoB2cErrorEntity::getMessage, keyword); // 添加模糊查询条件
                    }
                }

                // 按 updateTime 正序排列，并限制返回数量
                queryWrapper.orderByAsc(SoB2cErrorEntity::getUpdateTime);
                List<SoB2cErrorEntity> list = soB2cErrorService.list(queryWrapper.last(" LIMIT " + count));
                XxlJobHelper.log("SoB2cRetryJob 任务数量：{}", list.size());
                if (CollUtil.isEmpty(list)) {
                    XxlJobHelper.log("SoB2cRetryJob 需要执行任务列表为空");
                    return ReturnT.SUCCESS;
                }
                List<String> soIds = list.stream().map(SoB2cErrorEntity::getMainId).distinct().collect(Collectors.toList());
                Map<String, SoB2cEntity> soMap = soB2cService.listByIds(soIds)
                        .stream()
                        .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
                Map<String, SoB2cLogisticsEntity> logisticsEntityMap = soB2cLogisticsService.listByMainIds(soIds)
                        .stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getMainId, Function.identity()));
                // 根据订单ID分组去重
                Map<String, SoB2cErrorEntity> gourpErrorMap = list.stream()
                        .collect(Collectors.toMap(
                                SoB2cErrorEntity::getMainId,
                                entity -> entity,
                                (existing, replacement) -> existing));

                for (Map.Entry<String, SoB2cErrorEntity> entry : gourpErrorMap.entrySet()) {
                    SoB2cErrorEntity soB2cErrorEntity = entry.getValue();
                    XxlJobHelper.log("===============SoB2cRetryJob 当前任务执行：异常记录Id={}, 订单Id={}=========", soB2cErrorEntity.getId(), soB2cErrorEntity.getMainId());
                    try {
                        SoB2cEntity soB2cEntity = soMap.get(soB2cErrorEntity.getMainId());
                        if (Objects.isNull(soB2cEntity) || InvalidStatusEnum.VOIDED.getStatus().equals(soB2cEntity.getInvalidStatus()) || CharSequenceUtil.isBlank(soB2cEntity.getSignOrderError())){
                            soB2cErrorService.removeErrorOrder(soB2cErrorEntity.getMainId(),soB2cErrorEntity.getType());
                            //记录清除异常动作
                            operateLogService.addModuleOperateLog(CharSequenceUtil.format("【定时任务】清除销售订单异常标识【】记录",SoB2cErrorTypeEnum.getName(soB2cErrorEntity.getType())), ModuleTypeEnum.SO_B2C.getCode(), soB2cErrorEntity.getMainId(), "清除异常记录");
                            continue;
                        }
                        if (checkSignDelivery(soB2cErrorEntity, type, soMap, logisticsEntityMap,channelIdList)) {
                            XxlJobHelper.log("SoB2cRetryJob 当前任务执行处理：异常记录Id={}, 订单Id={}", soB2cErrorEntity.getId(), soB2cErrorEntity.getMainId());
                            soB2cErrorEntity.setRetryCount(soB2cErrorEntity.getRetryCount() + 1);
                            soB2cErrorService.updateById(soB2cErrorEntity);
                            continue;
                        };

                        List<BatchResultDTO> resultDTOS = soB2cAbnormalService.batchRetry(soB2cErrorEntity.getMainId());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            XxlJobHelper.log("SoB2cRetryJob 当前任务睡眠失败");
                            continue;
                        }
                        // 成功重新记录重试数量任务
                        boolean update = soB2cErrorService.lambdaUpdate()
                                .set(SoB2cErrorEntity::getRetryCount, soB2cErrorEntity.getRetryCount() + 1)
                                .eq(SoB2cErrorEntity::getMainId, soB2cErrorEntity.getMainId())
                                .eq(SoB2cErrorEntity::getType, soB2cErrorEntity.getType())
                                .update();
                        XxlJobHelper.log("SoB2cRetryJob 当前任务执行成功：{}, 重新记录数量结果={}", JSONUtil.toJsonStr(resultDTOS), update);
                    } catch (Exception e) {

                        log.error("SoB2cRetryJob 当前任务执行成功异常：soId={}, error={}",
                                soB2cErrorEntity.getMainId(),
                                ExceptionUtil.stacktraceToString(e)
                        );
                        XxlJobHelper.log("SoB2cRetryJob 当前任务执行成功异常：soB2cErrorId={}, error={}",
                                soB2cErrorEntity.getMainId(),
                                ExceptionUtil.stacktraceToString(e)
                        );
                        // 成功重新记录重试数量任务
                        boolean update = soB2cErrorService.lambdaUpdate()
                                .set(SoB2cErrorEntity::getRetryCount, soB2cErrorEntity.getRetryCount() + 1)
                                .eq(SoB2cErrorEntity::getMainId, soB2cErrorEntity.getMainId())
                                .eq(SoB2cErrorEntity::getType, soB2cErrorEntity.getType())
                                .update();
                        XxlJobHelper.log("SoB2cRetryJob 当前任务执行异常：{}, 重新记录数量结果={}", JSONUtil.toJsonStr(e.getMessage()), update);
                    }
                }
            }
            XxlJobHelper.log("SoB2cRetryJob 执行任务列表结束");
        } finally {
            if (redisTemplate.hasKey(redisKey)) {
                redisTemplate.opsForValue().getOperations().delete(redisKey);
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 检查当前销售订单是否可标记发货
     */
    private boolean checkSignDelivery(SoB2cErrorEntity soB2cErrorEntity, String type, Map<String, SoB2cEntity> soMap, Map<String, SoB2cLogisticsEntity> logisticsEntityMap, List<String> channelIdList) {
        if (SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode().equalsIgnoreCase(type)) {
            SoB2cEntity soB2cEntity = soMap.get(soB2cErrorEntity.getMainId());
            if (null == soB2cEntity) {
                XxlJobHelper.log("SoB2cRetryJob 当前任务无销售订单id={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (soB2cEntity.getIsCancel() || soB2cEntity.getInvalidStatus()) {
                XxlJobHelper.log("SoB2cRetryJob 当前任务销售订单作废={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (!SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equalsIgnoreCase(soB2cEntity.getBillStatus()) &&
                    !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equalsIgnoreCase(soB2cEntity.getBillStatus())
            ) {
                soB2cErrorEntity.setVersion(soB2cErrorEntity.getVersion() + 1);
                soB2cErrorService.updateById(soB2cErrorEntity);
                XxlJobHelper.log("SoB2cRetryJob 当前任务销售订单非待发货/已发货={}", soB2cErrorEntity.getMainId());
                return true;
            }
        }
        if (SoB2cErrorTypeEnum.SUBMIT_DELIVERY.getCode().equalsIgnoreCase(type)
                || SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode().equalsIgnoreCase(type)) {
            SoB2cEntity soB2cEntity = soMap.get(soB2cErrorEntity.getMainId());
            if (null == soB2cEntity) {
                XxlJobHelper.log("SoB2cRetryJob 当前任务无销售订单id={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (soB2cEntity.getIsCancel() || soB2cEntity.getInvalidStatus()) {
                XxlJobHelper.log("SoB2cRetryJob 当前任务销售订单作废={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (!(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equalsIgnoreCase(soB2cEntity.getBillStatus()) ||
                    ApproveStatusEnum.APPROVE.equals(soB2cEntity.getApproveStatus()))
            ) {
                soB2cErrorEntity.setVersion(soB2cErrorEntity.getVersion() + 1);
                soB2cErrorService.updateById(soB2cErrorEntity);
                XxlJobHelper.log("SoB2cRetryJob 当前任务销售订单非审核通过-配货中={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (soB2cEntity.getIsOutOfRangeDelivery()){
                XxlJobHelper.log("SoB2cRetryJob 当前任务超出范围={}", soB2cErrorEntity.getMainId());
                return true;
            }
            SoB2cLogisticsEntity soB2cLogisticsEntity = logisticsEntityMap.get(soB2cErrorEntity.getMainId());
            if (null == soB2cLogisticsEntity) {
                XxlJobHelper.log("SoB2cRetryJob 当前任务无销售订单物流信息id={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (CharSequenceUtil.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
                XxlJobHelper.log("SoB2cRetryJob 当前任务无渠道id={}", soB2cErrorEntity.getMainId());
                return true;
            }
            if (!channelIdList.contains(soB2cLogisticsEntity.getLogisticsChannelId())) {
                XxlJobHelper.log("SoB2cRetryJob 当前订单【{}】任务物流规则无匹配渠道名称={}", soB2cErrorEntity.getMainId(),soB2cLogisticsEntity.getLogisticsChannelName());
                return true;
            }

        }
        return false;
    }

}
