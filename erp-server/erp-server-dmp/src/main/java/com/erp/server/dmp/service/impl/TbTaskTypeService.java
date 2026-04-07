package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.model.dmp.enums.DmpInputNextLevelType;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TbTaskTypeService {
    @Resource
    private PlatformApiService platformApiService;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private CfgSettingService cfgSettingService;

    private Long timeoutSeconds;

    private Long timeoutMabangHours;
    @Resource
    private CreateRequestReportTaskService createRequestReportTaskService;

    @Resource
    private DmpBasicSystemService dmpBasicSystemService;

    @Resource
    private DmpCfgInputService dmpCfgInputService;

    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;

    @Resource
    private DmpCfgOutputService dmpCfgOutputService;

    @Resource
    private DmpCfgOutputDetailService dmpCfgOutputDetailService;

    @Resource
    private DmpCfgInputConvertService dmpCfgInputConvertService;

    @Value("${openApi.mabang.timeoutHour:24}")
    public void setTimeoutMabangHours(Long timeoutMabangHours) {
        this.timeoutMabangHours = timeoutMabangHours;
    }

    @Value("${openApi.timeoutSeconds:3600}")
    public void setTimeoutSeconds(Long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 定时查询需要拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/8 17:32
     * @return java.util.List<com.erp.server.entity.PlatformApiTaskEntity>
     **/
    public List<JobTaskDTO> getTask() {
        LocalDateTime localTime = LocalDateTime.now();
        // 查询任务列表
        List<JobTaskDTO> sourcejobTaskDTOList = platformApiTaskService.listApiTask(localTime, "pull");
        // 获取延时配置Map<apiCode, 延时秒数>
        Map<String, Integer> delayConfgMap = cfgSettingService.getApiTaskDelaySecond(SettingEnum.PLATFORM_API_TASK_DELAY_SECOND);
        // 过滤小于延时时间的任务
        List<JobTaskDTO> jobTaskDTOList = sourcejobTaskDTOList.stream()
                .filter(e -> {
                    // 执行中跳过校验
                    if (2 == e.getStatus()){
                        return true;
                    }
                    Integer delaySecond = delayConfgMap.get(e.getApiCode());
                    if (null == delaySecond){
                        // 配置不存在跳过
                        return true;
                    } else {
                        // 下次执行时间 + 延时时间 <= 当前时间
                        return !e.getNextTime().plusSeconds(delaySecond).isAfter(localTime);
                    }
                }).collect(Collectors.toList());

        // 任务量等于0，任务重新开始,分页设置成0
        if (CollectionUtil.isEmpty(jobTaskDTOList)) {
            return Collections.emptyList();
        }
        // 设置超时恢复状态
        List<JobTaskDTO> inProgressList = jobTaskDTOList.stream()
            .filter(task -> 1 == task.getStatus())
            .collect(Collectors.toList());
        List<JobTaskDTO> timeoutList = jobTaskDTOList.stream()
            .filter(task -> 2 == task.getStatus())
            .filter(task -> {
                LocalDateTime nextTime = task.getNextTime();
                LocalDateTime updateTime = task.getUpdateTime();
                if (task.getApiName().contains(TaskConstant.MABANG)) {
                    return localTime.isAfter(nextTime.plusHours(timeoutMabangHours))
                            && localTime.isAfter(updateTime.plusHours(timeoutMabangHours));
                } else {
                    // 检查和获取默认时间
                    Long currentTimeoutSeconds = task.getAndCheckTimeoutSeconds(timeoutSeconds);
                    return localTime.isAfter(nextTime.plusSeconds(currentTimeoutSeconds))
                            && localTime.isAfter(updateTime.plusSeconds(currentTimeoutSeconds));
                }
            }).collect(Collectors.toList());
        // 需要设置超时恢复的任务
        if (CollectionUtil.isNotEmpty(timeoutList)){
            // 移除缓存已有的数据
            for (JobTaskDTO jobTaskDTO : timeoutList) {
                redisTemplate.boundListOps(jobTaskDTO.getGroupId()).remove(0, JSONObject.toJSONString(jobTaskDTO));
            }
            platformApiTaskService.updateTaskTypeState(timeoutList, 1);
        }
        // 设置任务正在执行中
        if (CollectionUtil.isNotEmpty(inProgressList)){
            platformApiTaskService.updateTaskTypeState(inProgressList, 2);
        }
        return inProgressList;
    }

    /**
     * 定时添加平台任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:49
     * @return void
     **/
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public void addTask(ShopInfoEntity shopInfo) {
        // 查询需要当前平台需要增加的任务
//        platformApiTaskService.createOrEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(),shopInfo.getDictPlatform()));

        // 添加新中台任务
        addNewDmpTask(shopInfo);

        // 添加完成后，修改店铺生成任务状态
        Boolean result = shopInfoFeign.updateShopInfoById(new ShopInfoEntity(shopInfo.getId(), Boolean.TRUE));
    }


    /**
     * 三方仓添加新任务
     */
    public void addNewDmpTask(OverseasProviderEntity overseasProviderEntity) {
        //根据授权的系统编码查询新中台系统表
        String systemCode = resolveThirdWarehouseSystemCode(overseasProviderEntity.getCode());
        DmpBasicSystemEntity dmpBasicSystemEntity = dmpBasicSystemService.listByCode(systemCode);
        if (ObjectUtil.isEmpty(dmpBasicSystemEntity)) {
            log.error("三方仓授权编码【{}】映射系统编码【{}】在新中台系统表中不存在！",
                    overseasProviderEntity.getCode(), systemCode);
            return;
        }

        //获取系统id
        String systemId = dmpBasicSystemEntity.getId();

        //根据系统id查询所有主任务
        List<DmpCfgInputEntity> cfgInputEntityList = listThirdWarehouseCfgInputs(systemId, overseasProviderEntity.getCode());
        List<String> cfgInputIds = cfgInputEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(cfgInputIds)) {
            return;
        }

        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery().in(DmpCfgInputDetailEntity::getMainId, cfgInputIds).list();

        //每一个主任务都需要添加任务详情
        for (DmpCfgInputEntity dmpCfgInputEntity : cfgInputEntityList) {
            DmpCfgInputDetailEntity dmpCfgInputDetailEntity = list.stream().filter(req -> req.getNextLevelId().equals(overseasProviderEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dmpCfgInputDetailEntity)) {
                //添加输入任务
                addInputDetail(overseasProviderEntity, dmpCfgInputEntity);
            }
        }

        // 添加中台任务
        addDmpOutputDetail(overseasProviderEntity, cfgInputIds);

        // 添加restCloud任务
        addRestCloudOutputDetail(overseasProviderEntity, systemId);

    }

    private void addRestCloudOutputDetail(OverseasProviderEntity overseasProviderEntity, String systemId) {
        List<DmpCfgOutputEntity> outputEntityList = dmpCfgOutputService.lambdaQuery()
                .eq(DmpCfgOutputEntity::getExecSystem, DmpCfgInputExecSystemEnum.REST_CLOUD.getCode())
                .in(DmpCfgOutputEntity::getSystemId, systemId)
                .list();
        if (CollUtil.isEmpty(outputEntityList)) {
            return;
        }
        List<String> outputIds = outputEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<DmpCfgOutputDetailEntity> cfgOutputDetailEntities = dmpCfgOutputDetailService.lambdaQuery()
                .in(DmpCfgOutputDetailEntity::getMainId, outputIds)
                .list();

        //添加输出任务详情
        for (DmpCfgOutputEntity dmpCfgOutputEntity : outputEntityList) {
            DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = cfgOutputDetailEntities.stream().filter(req -> req.getNextLevelId().equals(overseasProviderEntity.getId())).findFirst().orElse(null);
            if (null == dmpCfgOutputDetailEntity) {
                DmpCfgOutputDetailDTO.AddDTO addDTO = new DmpCfgOutputDetailDTO.AddDTO();
                addDTO.setMainId(dmpCfgOutputEntity.getId());
                addDTO.setLastTime(LocalDateTime.now());
                addDTO.setNextTime(LocalDateTime.now().plusSeconds(600));
                addDTO.setIntervalTime(600);
                addDTO.setOverrideTime(10);
                addDTO.setMaxRetryCount(3);
                addDTO.setExecTimeout(1200);
                addDTO.setMaxIntervalTime(-1);
                addDTO.setDealyTime(10);
                addDTO.setNextLevelId(overseasProviderEntity.getId());
                dmpCfgOutputDetailService.add(addDTO);
            } else {
                dmpCfgOutputDetailEntity.setDisabled(false);
                dmpCfgOutputDetailService.updateById(dmpCfgOutputDetailEntity);
            }
        }

    }

    private void addDmpOutputDetail(OverseasProviderEntity overseasProviderEntity, List<String> cfgInputIds) {
        List<DmpCfgInputConvertEntity> cfgInputConvertEntities = dmpCfgInputConvertService.lambdaQuery().in(DmpCfgInputConvertEntity::getMainId, cfgInputIds).list();
        List<String> convertIds = cfgInputConvertEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        if (CollUtil.isEmpty(convertIds)) {
            return;
        }
        List<DmpCfgOutputEntity> outputEntityList = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getInputConvertId, convertIds).list();

        List<String> outputIds = outputEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(outputIds)) {
            return;
        }

        List<DmpCfgOutputDetailEntity> cfgOutputDetailEntities = dmpCfgOutputDetailService.lambdaQuery().in(DmpCfgOutputDetailEntity::getMainId, outputIds).list();

        //添加输出任务详情
        for (DmpCfgOutputEntity dmpCfgOutputEntity : outputEntityList) {
            DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = cfgOutputDetailEntities.stream().filter(req -> req.getNextLevelId().equals(overseasProviderEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dmpCfgOutputDetailEntity)) {
                DmpCfgOutputDetailDTO.AddDTO addDTO = new DmpCfgOutputDetailDTO.AddDTO();
                addDTO.setMainId(dmpCfgOutputEntity.getId());
                addDTO.setNextLevelId(overseasProviderEntity.getId());
                dmpCfgOutputDetailService.add(addDTO);
            }
        }
    }

    /**
     * 添加新中台任务
     * @Author Luo_WG
     * @Date 2024/9/19 9:23
     * @param shopInfo
     * @return void
     **/
    private void addNewDmpTask(ShopInfoEntity shopInfo) {
        //根据授权的系统编码查询新中台系统表
        // pgsql改成忽略大小写查询
        DmpBasicSystemEntity dmpBasicSystemEntity = dmpBasicSystemService.query()
                .apply("code ILIKE {0}", shopInfo.getDictPlatform().toLowerCase())
                .last("LIMIT 1")
                .one();
        if (ObjectUtil.isEmpty(dmpBasicSystemEntity)) {
            log.error("店铺授权编码【{}】 在新中台系统表中不存在！", shopInfo.getDictPlatform());
            return;
        }

        //获取系统id
        String systemId = dmpBasicSystemEntity.getId();

        //根据系统id查询所有主任务
        List<DmpCfgInputEntity> cfgInputEntityList = dmpCfgInputService.lambdaQuery()
                .eq(DmpCfgInputEntity::getSystemId, systemId)
                .eq(DmpCfgInputEntity::getIsMainTask, Boolean.TRUE)
                .list();

        List<String> cfgInputIds = cfgInputEntityList.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(cfgInputIds)) {
            log.info("店铺授权编码【{}】 在新中台系统表中不存在！", shopInfo.getDictPlatform());
            return;
        }
        //根据主任务id查询所有主任务详情
        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery().in(DmpCfgInputDetailEntity::getMainId, cfgInputIds).list();

        // 用nextLevelId分组，取记录数量最多的分组
        Map<String, List<DmpCfgInputDetailEntity>> groupByNextLevelId = list.stream().collect(Collectors.groupingBy(DmpCfgInputDetailEntity::getNextLevelId));
        String maxCountNextLevelId = groupByNextLevelId.entrySet().stream().max(Comparator.comparingInt(entry -> entry.getValue().size())).map(Map.Entry::getKey).orElse(null);
        List<DmpCfgInputDetailEntity> dmpCfgInputDetailList = groupByNextLevelId.get(maxCountNextLevelId);

        if (CollectionUtil.isNotEmpty(dmpCfgInputDetailList)) {
            List<String> sameAccountShopId = new ArrayList<>();
            // 判断是否存在账号相同的任务
            boolean hasSameAccountTask = list.stream().anyMatch(e -> DmpInputNextLevelType.OMS_ACCOUNT.getCode().equals(e.getNextLevelType()));
            if (hasSameAccountTask) {
                // 获取当前店铺的账号
                String currentShopAccount = shopInfo.getPlatformShopCode();
                // 获取账号相同的任务的店铺信息
                List<ShopInfoEntity> shopList = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null));
                sameAccountShopId = shopList.stream()
                        .filter(shop -> currentShopAccount.equals(shop.getPlatformShopCode()))
                        .map(BaseEntity::getId)
                        .collect(Collectors.toList());
            }

            // 判断当前店是否已经存在记录，如果存在则不复制
            List<String> finalSameAccountShopId = sameAccountShopId;
            Map<String, List<DmpCfgInputDetailEntity>> existCfgInputIdMap = list.stream()
                    .filter(e ->
                            e.getNextLevelId().equals(shopInfo.getId()) || (DmpInputNextLevelType.OMS_ACCOUNT.getCode().equals(e.getNextLevelType()) && finalSameAccountShopId.contains(e.getNextLevelId()))
                    )
                    .collect(Collectors.groupingBy(DmpCfgInputDetailEntity::getMainId));

            // 不存在的任务复制记录到新的nextLevelId
            List<DmpCfgInputDetailEntity> newInputDetails = dmpCfgInputDetailList
                    .stream()
                    .filter(e-> !existCfgInputIdMap.containsKey(e.getMainId()))
                    .map(inputDetail -> convertNewDmpCfgInputDetailEntity(shopInfo, inputDetail))
                    .collect(Collectors.toList());

            if (CollectionUtil.isNotEmpty(newInputDetails)) {
                // 批量插入新的记录
                dmpCfgInputDetailService.saveBatch(newInputDetails);
            }
        }


        //根据系统id查询所有主任务
        List<DmpCfgInputEntity> listAll = dmpCfgInputService.lambdaQuery()
                .eq(DmpCfgInputEntity::getSystemId, systemId)
                .list();
        List<String> inputIds = listAll.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());

        List<DmpCfgInputConvertEntity> cfgInputConvertEntities = dmpCfgInputConvertService.lambdaQuery().in(DmpCfgInputConvertEntity::getMainId, inputIds).list();
        List<String> convertIds = cfgInputConvertEntities.stream().map(BaseEntity::getId).collect(Collectors.toList());
        if (CollUtil.isEmpty(convertIds)) {
            return;
        }
        List<DmpCfgOutputEntity> outputEntityList = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getInputConvertId, convertIds).list();

        List<String> outputIds = outputEntityList.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(outputIds)) {
            return;
        }

        List<DmpCfgOutputDetailEntity> cfgOutputDetailEntities = dmpCfgOutputDetailService.lambdaQuery().in(DmpCfgOutputDetailEntity::getMainId, outputIds).list();

        //添加输出任务详情
        for (DmpCfgOutputEntity dmpCfgOutputEntity : outputEntityList) {
            DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = cfgOutputDetailEntities.stream().filter(req -> req.getNextLevelId().equals(shopInfo.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dmpCfgOutputDetailEntity)) {
                DmpCfgOutputDetailDTO.AddDTO addDTO = new DmpCfgOutputDetailDTO.AddDTO();
                addDTO.setMainId(dmpCfgOutputEntity.getId());
                addDTO.setNextLevelId(shopInfo.getId());
                dmpCfgOutputDetailService.add(addDTO);
            }
        }
    }


    /**
     * 添加输入任务
     * @param dmpCfgInputEntity
     */
    private void addInputDetail(OverseasProviderEntity overseasProviderEntity, DmpCfgInputEntity dmpCfgInputEntity) {

        boolean isCaiNiaoOutbound = overseasProviderEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode()) && (dmpCfgInputEntity.getCode().equals("outbound") || dmpCfgInputEntity.getCode().equals("inbound"));
        //添加基础任务
        DmpCfgInputDetailDTO.AddDTO addDTO = new DmpCfgInputDetailDTO.AddDTO();
        addDTO.setMainId(dmpCfgInputEntity.getId());
        addDTO.setNextLevelId(overseasProviderEntity.getId());
        if(isCaiNiaoOutbound){
            addDTO.setLastTime(LocalDateTime.now().plusYears(100));
            addDTO.setNextTime(LocalDateTime.now().plusYears(100));
        }else{
            addDTO.setLastTime(LocalDateTime.now());
            addDTO.setNextTime(LocalDateTime.now().plusSeconds(600));
        }
        addDTO.setIntervalTime(600);
        addDTO.setOverrideTime(120);
        addDTO.setMaxRetryCount(3);
        addDTO.setExecTimeout(1200);
        addDTO.setDealyTime(60);
        addDTO.setExtendJson(dmpCfgInputEntity.getExtendJson());
        addDTO.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
        dmpCfgInputDetailService.add(addDTO);
        if(!isCaiNiaoOutbound){
            //添加历史任务
            DmpCfgInputDetailDTO.AddDTO addHistoryDTO = new DmpCfgInputDetailDTO.AddDTO();
            addHistoryDTO.setMainId(dmpCfgInputEntity.getId());
            addHistoryDTO.setNextLevelId(overseasProviderEntity.getId());
            addHistoryDTO.setLastTime(overseasProviderEntity.getEnableDate().atStartOfDay());
            addHistoryDTO.setNextTime(overseasProviderEntity.getEnableDate().atStartOfDay().plusHours(6));
            addHistoryDTO.setIntervalTime(21600);
            addHistoryDTO.setOverrideTime(0);
            addHistoryDTO.setMaxRetryCount(3);
            addHistoryDTO.setExtendJson(dmpCfgInputEntity.getExtendJson());
            addHistoryDTO.setExecTimeout(1200);
            addHistoryDTO.setDealyTime(86400);
            addHistoryDTO.setTaskType(DmpInputTaskTaskTypeEnum.HISTORY.getCode());
            dmpCfgInputDetailService.add(addHistoryDTO);
        }
    }

    public List<LocalDateTime> splitTimeRange(LocalDateTime startTime, LocalDateTime endTime, Duration interval) {
        List<LocalDateTime> timeList = new ArrayList<>();
        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            timeList.add(current);
            current = current.plus(interval);
        }
        if (!current.equals(endTime)) {
            timeList.add(endTime);
        }
        return timeList;
    }

    public void removeThirdWarehouseTask(OverseasProviderEntity overseasProviderEntity) {
        //根据授权的系统编码查询新中台系统表
        String systemCode = resolveThirdWarehouseSystemCode(overseasProviderEntity.getCode());
        DmpBasicSystemEntity dmpBasicSystemEntity = dmpBasicSystemService.listByCode(systemCode);
        if (ObjectUtil.isEmpty(dmpBasicSystemEntity)) {
            log.error("三方仓授权编码【{}】映射系统编码【{}】在新中台系统表中不存在！",
                    overseasProviderEntity.getCode(), systemCode);
            return;
        }

        //获取系统id
        String systemId = dmpBasicSystemEntity.getId();

        //根据系统id查询所有主任务
        List<DmpCfgInputEntity> cfgInputEntityList = listThirdWarehouseCfgInputs(systemId, overseasProviderEntity.getCode());
        List<String> cfgInputIds = cfgInputEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(cfgInputIds)) {
            return;
        }

        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery()
                .in(DmpCfgInputDetailEntity::getMainId, cfgInputIds)
                .eq(DmpCfgInputDetailEntity::getNextLevelId, overseasProviderEntity.getId())
                .list();
        if(CollectionUtils.isNotEmpty(list)){
            dmpCfgInputDetailService.removeByIds(list.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        }

        // 中台
        removeDmpThirdWarehouseTask(overseasProviderEntity, cfgInputIds);

        // restCloud
        removeRestCloudThirdWarehouseTask(overseasProviderEntity, systemId);
    }

    private void removeRestCloudThirdWarehouseTask(OverseasProviderEntity overseasProviderEntity, String systemId) {
        List<DmpCfgOutputEntity> outputEntityList = dmpCfgOutputService.lambdaQuery()
                .eq(DmpCfgOutputEntity::getExecSystem, DmpCfgInputExecSystemEnum.REST_CLOUD.getCode())
                .in(DmpCfgOutputEntity::getSystemId, systemId)
                .list();
        if (CollUtil.isEmpty(outputEntityList)) {
            return;
        }
        List<String> outputIds = outputEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<DmpCfgOutputDetailEntity> cfgOutputDetailEntities = dmpCfgOutputDetailService.lambdaQuery()
                .in(DmpCfgOutputDetailEntity::getMainId, outputIds)
                .in(DmpCfgOutputDetailEntity::getNextLevelId, overseasProviderEntity.getId())
                .list();

        if (CollUtil.isNotEmpty(cfgOutputDetailEntities)){
            cfgOutputDetailEntities.forEach(e -> {e.setDisabled(true);});
            dmpCfgOutputDetailService.updateBatchById(cfgOutputDetailEntities);
        }


    }

    private void removeDmpThirdWarehouseTask(OverseasProviderEntity overseasProviderEntity, List<String> cfgInputIds) {
        List<DmpCfgInputConvertEntity> cfgInputConvertEntities = dmpCfgInputConvertService.lambdaQuery().in(DmpCfgInputConvertEntity::getMainId, cfgInputIds).list();
        List<String> convertIds = cfgInputConvertEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        if (CollUtil.isEmpty(convertIds)) {
            return;
        }
        List<DmpCfgOutputEntity> outputEntityList = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getInputConvertId, convertIds).list();

        List<String> outputIds = outputEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(outputIds)) {
            return;
        }

        List<DmpCfgOutputDetailEntity> cfgOutputDetailEntities = dmpCfgOutputDetailService.lambdaQuery()
                .in(DmpCfgOutputDetailEntity::getMainId, outputIds)
                .eq(DmpCfgOutputDetailEntity::getNextLevelId, overseasProviderEntity.getId())
                .list();

        if(CollectionUtils.isNotEmpty(cfgOutputDetailEntities)){
            dmpCfgOutputDetailService.removeByIds(cfgOutputDetailEntities.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        }
    }

    private String resolveThirdWarehouseSystemCode(String providerCode) {
        if (OmsPlatformEnum.FBT.getCode().equals(providerCode)) {
            return DmpBasicSystemCodeEnum.TIKTOK.getCode();
        }
        return providerCode;
    }

    private List<DmpCfgInputEntity> listThirdWarehouseCfgInputs(String systemId, String providerCode) {
        if (OmsPlatformEnum.FBT.getCode().equals(providerCode)) {
            return dmpCfgInputService.lambdaQuery()
                    .eq(DmpCfgInputEntity::getSystemId, systemId)
                    .eq(DmpCfgInputEntity::getDisabled, false)
                    .likeRight(DmpCfgInputEntity::getCode, "fbt")
                    .list();
        }
        return dmpCfgInputService.lambdaQuery()
                .eq(DmpCfgInputEntity::getSystemId, systemId)
                .eq(DmpCfgInputEntity::getDisabled, false)
                .list();
    }

    private static DmpCfgInputDetailEntity convertNewDmpCfgInputDetailEntity(ShopInfoEntity shopInfo, DmpCfgInputDetailEntity inputDetail) {
        DmpCfgInputDetailEntity newInputDetail = new DmpCfgInputDetailEntity();
        BeanUtils.copyProperties(inputDetail, newInputDetail);
        // 生成新的ID
        newInputDetail.setId(IdWorker.getIdStr());
        newInputDetail.setCreateTime(LocalDateTime.now());
        newInputDetail.setUpdateTime(LocalDateTime.now());
        LocalDateTime lastTime = LocalDateTime.now();
        if (DmpInputTaskTaskTypeEnum.HISTORY.getCode().equals(inputDetail.getTaskType()) && null != shopInfo.getInitPullTime()) {
            lastTime = shopInfo.getInitPullTime();
        }
        // 如果是历史任务，按店铺配置类
        newInputDetail.setLastTime(lastTime);
        newInputDetail.setNextTime(lastTime.plusSeconds(inputDetail.getIntervalTime()));

        // 设置新的nextLevelId
        newInputDetail.setNextLevelId(shopInfo.getId());
        return newInputDetail;
    }
}
