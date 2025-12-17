package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.dmp.entity.PlatformApiEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.ShopPlatformStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.mapper.PlatformApiTaskMapper;
import com.erp.server.dmp.service.AmzReportScheduleService;
import com.erp.server.dmp.service.PlatformApiService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Cloud
 */
@Slf4j
@Service
public class PlatformApiTaskServiceImpl extends SuperServiceImpl<PlatformApiTaskMapper, PlatformApiTaskEntity>
        implements PlatformApiTaskService {

    @Resource
    private PlatformApiService platformApiService;
    @Resource
    private AmzReportScheduleService amzReportScheduleService;
    @Resource


    private ShopInfoFeign shopInfoFeign;

    /**
     * 修改任务下次执行
     *
     * @param jobTaskDTO jobTaskDTO
     * @param type
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     **/
    @Override
    public Boolean updateTaskStateById(JobTaskDTO jobTaskDTO, Integer type) {
        LambdaUpdateWrapper<PlatformApiTaskEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        if (1 != type) {
            Integer interval = jobTaskDTO.getIntervalTime();
            LocalDateTime nextTime = jobTaskDTO.getNextTime();
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getLastTime, nextTime);
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getNextTime, nextTime.plusSeconds(interval));
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryTimes, 0);
        } else {
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryTimes, jobTaskDTO.getRetryTimes() + 1);
        }
        if (3 != type) {
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getStatus, 1);
        }
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getUpdateTime, LocalDateTime.now());
        lambdaUpdateWrapper.eq(PlatformApiTaskEntity::getId, jobTaskDTO.getId());
        return this.update(lambdaUpdateWrapper);
    }

    @Override
    public PlatformApiTaskEntity getByApiCode(String taskName) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getApiCode, taskName)
                .le(PlatformApiTaskEntity::getNextTime, LocalDateTime.now())
                .eq(PlatformApiTaskEntity::getStatus, 3)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrEnablePlatformTask(PlatformTaskDTO.AddDTO dto) {
        List<PlatformApiEntity> entityList = platformApiService.listByPlatform(dto.getDictPlatform());
        if (CollectionUtil.isEmpty(entityList)) {
            return Boolean.TRUE;
        }
        // 根据店铺id查询是否已经存在任务
        List<PlatformApiTaskEntity> taskEntity = lambdaQuery()
                .eq(PlatformApiTaskEntity::getShopId, dto.getShopId())
                .eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .list();
        // 对比当前店铺不存在的任务
        Set<String> existApiIds = taskEntity.stream().map(PlatformApiTaskEntity::getPlatformApiId).collect(Collectors.toSet());
        // 需要添加的任务
        List<PlatformApiEntity> notExistApiList = entityList
                .stream()
                .filter(item -> !existApiIds.contains(item.getId()))
                .collect(Collectors.toList());
        // 需要更新的任务
        List<PlatformApiTaskEntity> updateList = taskEntity.stream().filter(PlatformApiTaskEntity::getDisabled).collect(Collectors.toList());

        List<PlatformApiTaskEntity> insertEntityList = notExistApiList.stream()
                .map(task -> getPlatformApiTaskEntity(dto, task))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(insertEntityList)) {
            this.saveBatch(insertEntityList);
        }
        if (CollectionUtil.isNotEmpty(updateList)) {
            updateList.forEach(e-> e.setDisabled(false));
            this.updateBatchById(updateList);
        }
        return Boolean.TRUE;
    }

    private static PlatformApiTaskEntity getPlatformApiTaskEntity(PlatformTaskDTO.AddDTO dto, PlatformApiEntity task) {
        PlatformApiTaskEntity entity = new PlatformApiTaskEntity();
        entity.setShopId(dto.getShopId());
        entity.setShopName(dto.getShopName());
        entity.setDictPlatform(dto.getDictPlatform());
        entity.setApiCode(task.getApiCode());
        entity.setApiName(task.getApiName());
        entity.setIntervalTime(task.getIntervalTime());
        entity.setLastTime(LocalDateTime.now());
        entity.setNextTime(LocalDateTime.now().plusSeconds(task.getIntervalTime()));
        entity.setStatus(1);
        entity.setRetryTimes(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setPlatformApiId(task.getId());
        entity.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        entity.setSyncOperate(task.getSyncOperate());
        entity.setBillType(task.getBillType());
        entity.setOperateType(task.getOperateType());
        entity.setDisabled(task.getDisabled());
        entity.setTimeoutSeconds(task.getTimeoutSeconds());
        // 分组ID
        String groupId = task.getDictPlatform();
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(task.getDictPlatform());
        // 平台不存在
        if (null == platformDictEnum){
            entity.setGroupId(groupId);
            return entity;
        }
        // 店铺不存在
        String shopId = dto.getShopId();
        if (StringUtils.isBlank(shopId)){
            entity.setGroupId(groupId);
            return entity;
        }
        if (PlatformDictEnum.AMAZON == platformDictEnum){
            // 亚马逊平台自定义分组ID：平台:卖家ID:业务类型
            groupId = StrUtil.format("{}:{}:{}", task.getDictPlatform(), dto.getPlatformShopCode(), task.getBillType());
            entity.setGroupId(groupId);
            return entity;
        }
        // 其他平台分组ID规则:平台:店铺ID
        groupId = StrUtil.format("{}:{}", task.getDictPlatform(), shopId);
        entity.setGroupId(groupId);
        entity.setDisabled(task.getDisabled());
        return entity;
    }

    @Override
    public List<PlatformApiTaskEntity> listByPlatformAndShop(String dictPlatform, String shopId) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getDictPlatform, dictPlatform)
                .eq(PlatformApiTaskEntity::getShopId, shopId)
                .list();
    }

    @Override
    public List<JobTaskDTO> listApiTask(LocalDateTime localTime, String operateType) {
        return baseMapper.selectApiTask(localTime, operateType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskTypeState(List<JobTaskDTO> timeoutList, int type) {
        baseMapper.updateTaskTypeState(timeoutList, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removePlatformTask(PlatformTaskDTO.AddDTO dto) {
        LambdaQueryWrapper<PlatformApiTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform());
        queryWrapper.eq(PlatformApiTaskEntity::getShopId, dto.getShopId());
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disabledPlatformTask(PlatformTaskDTO.DisabledDTO dto) {
        return lambdaUpdate().eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .eq(PlatformApiTaskEntity::getShopId, dto.getShopId())
                .ne(PlatformApiTaskEntity::getDisabled, dto.getDisabled())
                .set(PlatformApiTaskEntity::getDisabled, dto.getDisabled())
                .update(new PlatformApiTaskEntity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createThirdWarehouseTask(ThirdWarehouseTaskDTO.AddDTO dto) {
        List<PlatformApiEntity> entityList = platformApiService.listByPlatform(dto.getDictPlatform());
        if (CollectionUtil.isEmpty(entityList)) {
            return Boolean.TRUE;
        }
        // 根据授权id查询
        List<PlatformApiTaskEntity> taskEntity = lambdaQuery()
                .eq(PlatformApiTaskEntity::getShopId, dto.getAuthKey())
                .eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .list();
        Set<String> existApiIds = taskEntity.stream().map(PlatformApiTaskEntity::getPlatformApiId).collect(Collectors.toSet());
        // 需要添加的任务
        List<PlatformApiEntity> notExistApiList = entityList
                .stream()
                .filter(item -> !existApiIds.contains(item.getId()))
                .collect(Collectors.toList());

        List<PlatformApiTaskEntity> insertEntityList = notExistApiList.stream()
                .map(task -> getThirdWarehouseApiTaskEntity(dto, task))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(insertEntityList)) {
            this.saveBatch(insertEntityList);
        }
        return Boolean.TRUE;
    }


    private static PlatformApiTaskEntity getThirdWarehouseApiTaskEntity(ThirdWarehouseTaskDTO.AddDTO dto, PlatformApiEntity task) {
        PlatformApiTaskEntity entity = new PlatformApiTaskEntity();
        entity.setShopId(dto.getAuthKey());
        entity.setShopName(task.getDictPlatform());
        entity.setApiParam(dto.getAuthInfo());
        entity.setDictPlatform(dto.getDictPlatform());
        entity.setApiCode(task.getApiCode());
        entity.setApiName(task.getApiName());
        entity.setIntervalTime(task.getIntervalTime());
        //间隔一天，从0点开始执行
        if(task.getIntervalTime().equals(86400)){
            entity.setLastTime(LocalDateTimeUtil.beginOfDay(LocalDateTime.now()).minusSeconds(task.getIntervalTime()));
            entity.setNextTime(LocalDateTimeUtil.beginOfDay(LocalDateTime.now()));
        }else{
            //产品数据拉取全量
            LocalDateTime baseTime = (task.getBillType().equals(BusinessTypeEnum.PRODUCT.getCode())) ?
                    LocalDateTime.parse("2015-01-01T00:00:00") :
                    LocalDateTime.now().minusSeconds(task.getIntervalTime());

            entity.setLastTime(baseTime);
            entity.setNextTime(LocalDateTime.now());
        }
        entity.setStatus(1);
        entity.setRetryTimes(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setPlatformApiId(task.getId());
        entity.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        entity.setSyncOperate(task.getSyncOperate());
        entity.setBillType(task.getBillType());
        entity.setOperateType(task.getOperateType());
        return entity;
    }

    @Override
    public List<String> findGroupIdByPlatform(String dictPlatform) {
        LambdaQueryWrapper<PlatformApiTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PlatformApiTaskEntity::getGroupId);
        queryWrapper.eq(PlatformApiTaskEntity::getDictPlatform, dictPlatform);
        queryWrapper.eq(PlatformApiTaskEntity::getIsDeleted, Boolean.FALSE);
        queryWrapper.eq(PlatformApiTaskEntity::getDisabled, Boolean.FALSE);
        queryWrapper.groupBy(PlatformApiTaskEntity::getGroupId);
        return listObjs(queryWrapper, Object::toString);
    }

    @Override
    public List<PlatformApiTaskEntity> listByPlatformAndBillType(String dictPlatform, String billType) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getDictPlatform, dictPlatform)
                .eq(PlatformApiTaskEntity::getBillType, billType)
                .eq(PlatformApiTaskEntity::getIsDeleted, Boolean.FALSE)
                .eq(PlatformApiTaskEntity::getDisabled, Boolean.FALSE)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean allAddOrUpdateTaskAndSchedule(PlatformTaskDTO.DisabledDTO dto) {
        if (dto.getDisabled()){
            // 禁用
            this.disabledPlatformTask(dto);
            // 亚马逊取消任务计划
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform())){
                amzReportScheduleService.cancelReportSchedule(dto.getShopId());
            }
        } else {
            // 启用
            PlatformTaskDTO.AddDTO addDTO = new PlatformTaskDTO.AddDTO(dto.getShopId(), dto.getShopName(), dto.getDictPlatform(), dto.getPlatformShopCode());
            this.createOrEnablePlatformTask(addDTO);
            // 亚马逊启用任务计划
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform())){
                amzReportScheduleService.addOrUpdateReportSchedule(dto);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void checkAndClosedPlatformShop(ShopInfoEntity shopInfo) {
        if (shopInfo.getDisabled()){
            return;
        }
        shopInfo.setIsGenTask(Boolean.FALSE);
        shopInfo.setDisabled(true);
        shopInfo.setPlatformStatus(ShopPlatformStatusEnum.CLOSED.getCode());
        shopInfoFeign.updateShopInfoById(shopInfo);

        // 禁用启用任务和取消报告计划任务
        this.allAddOrUpdateTaskAndSchedule(new PlatformTaskDTO.DisabledDTO(shopInfo.getId(),
                shopInfo.getName(),
                shopInfo.getDictPlatform(),
                true,
                shopInfo.getDictCountryCode(),
                shopInfo.getPlatformShopCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void checkAndClosedPlatformShopByShopId(String shopId) {
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
        if (null == shopInfoEntity){
            throw new ServiceException("店铺不存在：id=" + shopId);
        }
        this.checkAndClosedPlatformShop(shopInfoEntity);
    }
}
