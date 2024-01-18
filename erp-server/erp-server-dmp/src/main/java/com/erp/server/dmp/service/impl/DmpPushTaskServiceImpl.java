package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpPushTaskExportExcelDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.mapper.DmpPushTaskMapper;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Slf4j
@Service
public class DmpPushTaskServiceImpl extends SuperServiceImpl<DmpPushTaskMapper, DmpPushTaskEntity> implements DmpPushTaskService {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMqAndSaveTask(DmpPushTaskFeignDTO dto) {
        // 保存任务表
        DmpPushTaskEntity entity = new DmpPushTaskEntity(dto);

        //查询来源上级单据
        Boolean isSend = isSendParentBillTask(entity);
        String entityId = saveOrUpdateDmpSyncTask(entity);
        //判断是否存在上级单据，并且推送成功
        if (!isSend) {
            return;
        }
        // 发送MQ消息
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(entityId, dto.getMqData());
        String mqData = dmpSyncMqDTO.getMqData();
        JSONObject jsonObject = JSONUtil.parseObj(mqData);
        jsonObject.set("dmpSyncTaskId",entityId);
        SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), JSONUtil.toJsonStr(jsonObject), entity.getSourceId());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        LambdaUpdateWrapper<DmpPushTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DmpPushTaskEntity::getId, paramDTO.getDmpSyncTaskId());
        updateWrapper.set(DmpPushTaskEntity::getLastSyncTime, LocalDateTime.now());
        updateWrapper.set(DmpPushTaskEntity::getStatus, paramDTO.getSyncStatus());
        updateWrapper.set(StrUtil.isNotBlank(paramDTO.getResponseMsg()), DmpPushTaskEntity::getReturnMsg, paramDTO.getResponseMsg());
        updateWrapper.set(DmpPushTaskEntity::getUpdateTime, LocalDateTime.now());
        this.update(updateWrapper);

    }

    @Override
    public List<DmpPushTaskEntity> listNeedPushTask() {
        List<DmpPushTaskEntity> list = lambdaQuery()
                .in(DmpPushTaskEntity::getStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .list();
        return list;
    }

    @Override
    public DmpPushTaskEntity getByParam(DmpSyncTaskDTO.OneDTO oneDTO) {
        DmpPushTaskEntity found = lambdaQuery()
                .eq(DmpPushTaskEntity::getSourceId, oneDTO.getSourceId())
                .eq(StringUtils.isNotBlank(oneDTO.getSourceType()),DmpPushTaskEntity::getSourceType, oneDTO.getSourceType())
                .eq(DmpPushTaskEntity::getSourcePlatformName, oneDTO.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, oneDTO.getTargetPlatformName())
                .eq(StringUtils.isNotBlank(oneDTO.getMqTopic()),DmpPushTaskEntity::getMqTopic, oneDTO.getMqTopic())
                .eq(StringUtils.isNotBlank(oneDTO.getMqTag()),DmpPushTaskEntity::getMqTag, oneDTO.getMqTag())
                .last("LIMIT 1")
                .one();
        return found;
    }

    @Override
    public List<DmpPushTaskEntity> listByParam(DmpSyncTaskDTO.ListDTO listDTO) {
        List<DmpPushTaskEntity> list = lambdaQuery()
                .in(DmpPushTaskEntity::getSourceId, listDTO.getSourceIdList())
                .eq(StringUtils.isNotBlank(listDTO.getSourceType()), DmpPushTaskEntity::getSourceType, listDTO.getSourceType())
                .eq(DmpPushTaskEntity::getSourcePlatformName, listDTO.getSourcePlatformName())
                .eq(DmpPushTaskEntity::getTargetPlatformName, listDTO.getTargetPlatformName())
                .eq(StringUtils.isNotBlank(listDTO.getMqTopic()), DmpPushTaskEntity::getMqTopic, listDTO.getMqTopic())
                .eq(StringUtils.isNotBlank(listDTO.getMqTag()), DmpPushTaskEntity::getMqTag, listDTO.getMqTag())
                .list();
        return list;
    }

    @Override
    public List<DmpPushTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<DmpPushTaskDTO.TabListDTO> result = new ArrayList<>(4);
        List<DmpPushTaskDTO.TabListDTO> countList = baseMapper.listStatusCount(dto.getPermissionSql());
        //全部
        int allCount = countList.stream().mapToInt(DmpPushTaskDTO.TabListDTO::getCount).sum();
        DmpPushTaskDTO.TabListDTO all = new DmpPushTaskDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(DmpConstant.ALL);
        result.add(all);

        //同步成功
        DmpPushTaskDTO.TabListDTO success = new DmpPushTaskDTO.TabListDTO();
        success.setTabFlag(SyncStatusEnum.SUCCESS_SYNC.getCode());
        int successCount = countList.stream().filter(a -> a.getTabFlag().equals(success.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        success.setCount(successCount);
        result.add(success);

        //同步失败
        DmpPushTaskDTO.TabListDTO failed = new DmpPushTaskDTO.TabListDTO();
        failed.setTabFlag(SyncStatusEnum.FAILED_SYNC.getCode());
        int failedCount = countList.stream().filter(a -> a.getTabFlag().equals(failed.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        failed.setCount(failedCount);
        result.add(failed);

        //同步中
        DmpPushTaskDTO.TabListDTO syncIng = new DmpPushTaskDTO.TabListDTO();
        syncIng.setTabFlag(SyncStatusEnum.IN_SYNC.getCode());
        int syncIngCount = countList.stream().filter(a -> a.getTabFlag().equals(syncIng.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        syncIng.setCount(syncIngCount);
        result.add(syncIng);
        return result;
    }

    @Override
    public PagingVO<DmpPushTaskDTO.ListDTO> paging(PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        DmpPushTaskDTO.ParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<DmpPushTaskDTO.ListDTO> records = pageData.getRecords();
        //数据处理
        doOpHandleDmpPushTask(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(DmpPushTaskDTO.ParamDTO dto, HttpServletResponse response) {
        List<DmpPushTaskDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //数据处理
        doOpHandleDmpPushTask(list);
        List<DmpPushTaskExportExcelDTO> resultList = BeanMapperUtils.copyList(DmpPushTaskExportExcelDTO.class, list);
        String fileName = "中台推送任务表";
        try {
            ExcelUtil.export(fileName, "中台推送任务表", resultList, DmpPushTaskExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchSync(List<String> ids) {
        List<DmpPushTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        //需要修改备注信息
        List<DmpPushTaskEntity> updateList = new ArrayList<>();
        for (DmpPushTaskEntity dmpPushTaskEntity : list) {
            try {
                // 发送MQ消息
                DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpPushTaskEntity.getId(), dmpPushTaskEntity.getMqData());
                //查询来源上级单据
                Boolean isSend = isSendParentBillTask(dmpPushTaskEntity);
                //判断是否存在上级单据，并且推送成功
                if (!isSend) {
                    updateList.add(dmpPushTaskEntity);
                    continue;
                }
                String mqData = dmpSyncMqDTO.getMqData();
                JSONObject jsonObject = JSONUtil.parseObj(mqData);
                jsonObject.set("dmpSyncTaskId",dmpPushTaskEntity.getId());
                SendResult result = mqProducerService.syncClassMsg(dmpPushTaskEntity.getMqTopic(), dmpPushTaskEntity.getMqTag(), JSONUtil.toJsonStr(jsonObject), dmpPushTaskEntity.getSourceId());
                if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                    throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                }
            }catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(dmpPushTaskEntity.getSourceType());
                log.error("从{}推送{}到{}发送消息异常", dmpPushTaskEntity.getSourcePlatformName(), sourceTypeName, dmpPushTaskEntity.getTargetPlatformName(), e);
            }
        }
        //更新信息
        if (CollectionUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchFindDataSync(List<String> ids) {
        List<DmpPushTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        long count = list.stream().filter(obj -> !PlatformEnum.ERP.getDesc().equals(obj.getSourcePlatformName()) || !PlatformEnum.KINGDEE.getDesc().equals(obj.getTargetPlatformName())).count();
        if (count > 0) {
            throw new ServiceException(new ApiResult(10000,"只允许推送自研ERP>>>>金蝶的数据"));
        }
        Map<String, List<DmpPushTaskEntity>> map = list.stream().collect(Collectors.groupingBy(DmpPushTaskEntity::getSourceType));
        for (Map.Entry<String, List<DmpPushTaskEntity>> entry : map.entrySet()) {
            String sourceType = entry.getKey();
            List<DmpPushTaskEntity> value = entry.getValue();
            List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList = value.stream().map(obj -> new DmpSyncMqDTO.SyncParamDetailDTO(obj.getSourceId(), obj.getSyncOperate())).collect(Collectors.toList());
            try {
                // 发送MQ消息
                findDataAndSendMq(paramDetailList,sourceType);
            }catch (Exception e){
                String sourceTypeName = SourceTypeEnum.getName(sourceType);
                log.error("从{}推送{}到{}发送消息异常", PlatformEnum.ERP.getDesc(), sourceTypeName, PlatformEnum.KINGDEE.getDesc(), e);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {
        DmpPushTaskEntity entity = this.getById(syncTaskId);
        if (ObjectUtil.isEmpty(entity)) {
            return;
        }
        //查询redis,预警8小时发送一次
        String existKey = StrUtil.format(RedisKeyConstant.DMP_PUSH_TASK_WARN, entity.getId());
        boolean isHas = redisUtil.hasKey(existKey);
        if (isHas) {
            return;
        } else {
            //添加缓存
            redisUtil.set(existKey,entity, RedisService.EIGHT_HOURS_CACHE_TIME);
        }
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(entity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_OMS);
        warnMsgInfo.setTitle(StrUtil.format("单据【{}】从{}推送至{}失败",entity.getSourceCode(),entity.getSourcePlatformName(),entity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(entity.getSourceType()));
        warnMsgInfo.setTableId(entity.getSourceId());
        warnMsgInfo.setKeyInfo("");
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }


    @Override
    public Boolean isSendParentBillTask (DmpPushTaskEntity entity) {
        if (StrUtil.isBlank(entity.getParentId())) {
            return Boolean.TRUE;
        }
        DmpPushTaskEntity dmpPushTaskEntity = this.lambdaQuery().eq(DmpPushTaskEntity::getSourceId,entity.getParentId()).last("limit 1").one();

        if (ObjectUtil.isEmpty(dmpPushTaskEntity)) {
            entity.setReturnMsg("未找到上级单据推送任务");
            entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            return Boolean.FALSE;
        }
        if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(dmpPushTaskEntity.getStatus())) {
            entity.setReturnMsg("上级单据未推送成功，不支持推送下级单据");
            entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            return Boolean.FALSE;
        }
        return  Boolean.TRUE;
    }


    /**
     * @description: 重新查询数据发送MQ
     * @author Will
     * @date: 2023/10/30 10:03
     */
    private void findDataAndSendMq (List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList,String sourceType) {
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(sourceType);
        DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO(paramDetailList,sourceTypeEnum);
        switch (SourceTypeEnum.getEnum(sourceType)) {
            case BASIC_CATEGORY:
            case PRODUCT_DETAIL:
            case PRODUCT_BOM_INFO:
                plmTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case SYS_USER_INFO:
                sysUserFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case PURCHASE_ORDER:
            case PURCHASE_CHANGE:
            case PURCHASE_PRICE:
            case PURCHASE_PRICE_CHANGE:
            case SUBCONTRACT_CHANGE:
            case SUBCONTRACT_ORDER:
            case SUPPLIER:
                scmTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case MACHINE_INFO:
            case OTHER_OUTSTOCK:
            case OTHER_INSTOCK:
            case PO_INSTOCK:
            case PO_RECEIVE:
            case PO_RETURN:
            case SO_OUTSTOCK:
            case SO_RETURN_INSTOCK:
            case STOCKTAKING_PROFIT_LOSS:
            case TRANSFER_INFO:
            case WAREHOUSE:
                wmsTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            case CUSTOMER_INFO:
            case CUSTOMER_CONTACT:
            case CUSTOMER_GROUP:
            case SO_INFO:
            case SO_CHANGE:
                omsTaskFeign.findDataSendSyncTask(syncParamDTO);
                return;
            default:
                return;
        }
    }

    /**
     * @description: 列表查询数据格式话
     * @author Will
     * @date: 2023/10/13 15:05
     * @param list
     */
    private void doOpHandleDmpPushTask (List<DmpPushTaskDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DmpPushTaskDTO.ListDTO listDTO : list) {
            listDTO.setSyncTypeName("推送");
            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //同步状态名称
            listDTO.setStatusName(SyncStatusEnum.getNameByCode(listDTO.getStatus()));
            //同步操作名称
            listDTO.setSyncOperateName(SyncOperateEnum.getDescByCode(listDTO.getSyncOperate()));
        }
    }

    /**
     * 新增或修改
     */
    @Override
    public String saveOrUpdateDmpSyncTask(DmpPushTaskEntity entity) {
        DmpSyncTaskDTO.OneDTO map = BeanMapperUtils.map(DmpSyncTaskDTO.OneDTO.class, entity);
        DmpPushTaskEntity found = getByParam(map);
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            entity.setId(found.getId());
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
        }
        this.saveOrUpdate(entity);
        return entity.getId();
    }


}
