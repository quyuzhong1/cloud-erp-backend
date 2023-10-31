package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.excel.DmpPullTaskExportExcelDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.dmp.mapper.DmpPullTaskMapper;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 中台同步任务表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpPullTaskServiceImpl extends SuperServiceImpl<DmpPullTaskMapper, DmpPullTaskEntity> implements DmpPullTaskService {

    @Autowired
    private DmpPullTaskMapper dmpPullTaskMapper;

    @Resource
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSyncInfo(String id, String syncStatus, String responseMsg) {
        LambdaUpdateWrapper<DmpPullTaskEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DmpPullTaskEntity::getId, id);
        updateWrapper.set(DmpPullTaskEntity::getLastSyncTime, LocalDateTime.now());
        updateWrapper.set(DmpPullTaskEntity::getStatus, syncStatus);
        updateWrapper.set(StrUtil.isNotBlank(responseMsg), DmpPullTaskEntity::getReturnMsg, responseMsg);
        updateWrapper.set(DmpPullTaskEntity::getUpdateTime, LocalDateTime.now());
        this.update(updateWrapper);
    }

    @Override
    public void saveOrUpdateDmpSyncTask(DmpPullTaskEntity dmpSyncTaskEntity) {
        DmpPullTaskEntity found = lambdaQuery()
                .eq(DmpPullTaskEntity::getSourceType, dmpSyncTaskEntity.getSourceType())
                .eq(DmpPullTaskEntity::getSourceId, dmpSyncTaskEntity.getSourceId())
                .eq(DmpPullTaskEntity::getSourcePlatformName, dmpSyncTaskEntity.getSourcePlatformName())
                .eq(DmpPullTaskEntity::getTargetPlatformName, dmpSyncTaskEntity.getTargetPlatformName())
                .eq(DmpPullTaskEntity::getMqTopic, dmpSyncTaskEntity.getMqTopic())
                .eq(DmpPullTaskEntity::getMqTag, dmpSyncTaskEntity.getMqTag())
                .last("LIMIT 1")
                .one();
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            dmpSyncTaskEntity.setId(found.getId());
        }
        this.saveOrUpdate(dmpSyncTaskEntity);
    }

    /**
     * 新增同步金蝶退货单到wms退货入库单的任务
     * @Author Luo_WG
     * @Date 2023/7/4 19:48
     * @param entity
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeReturnOrderToWms(KingdeeReturnOrderEntity entity) {
        //新增发送任务
        DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskEntity.setSourceType(SourceTypeEnum.SAL_RETURNSTOCK.getCode());
        dmpSyncTaskEntity.setSourceId(entity.getFId());
        dmpSyncTaskEntity.setSourceCode(entity.getFBillNo());
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setStatus(SyncStatusEnum.IN_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(entity);
        dmpSyncTaskEntity.setMqData(mqData);
        this.saveOrUpdateDmpSyncTask(dmpSyncTaskEntity);
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public List<String> listKingdeeCode(Map<String, Object> conditon) {
        List<String> result= new ArrayList<>();

        LambdaQueryWrapper<DmpPullTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DmpPullTaskEntity::getSourceCode);
        queryWrapper.eq(DmpPullTaskEntity::getSourcePlatformName,"金蝶云星空")
                .eq(DmpPullTaskEntity::getTargetPlatformName,"自研ERP")
                .eq(null!=conditon.get("id"), DmpPullTaskEntity::getId, conditon.get("id"))
                .eq(null!=conditon.get("is_deleted"), DmpPullTaskEntity::getIsDeleted, conditon.get("is_deleted"))
                .eq(null!=conditon.get("source_type"), DmpPullTaskEntity::getSourceType, conditon.get("source_type"))
                .eq(null!=conditon.get("source_code"), DmpPullTaskEntity::getSourceCode, conditon.get("source_code"))
                .eq(null!=conditon.get("source_id"), DmpPullTaskEntity::getSourceCode, conditon.get("source_id"))
                .eq(null!=conditon.get("status"), DmpPullTaskEntity::getStatus, conditon.get("status"))
                .eq(null!=conditon.get("mq_tag"), DmpPullTaskEntity::getMqTag, conditon.get("mq_tag"))
                .like(null!=conditon.get("return_msg"), DmpPullTaskEntity::getReturnMsg, conditon.get("return_msg"))
        ;
        queryWrapper.last(null!=conditon.get("lastSql")," and " + conditon.get("lastSql").toString());
        List<DmpPullTaskEntity> queryResult=this.list(queryWrapper);

        if(CollectionUtil.isNotEmpty(queryResult)) {
            queryResult.stream().forEach(item-> result.add(item.getSourceCode()));
        }

        return result;
    }

    @Override
    public List<DmpPullTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<DmpPullTaskDTO.TabListDTO> result = new ArrayList<>(4);
        List<DmpPullTaskDTO.TabListDTO> countList = baseMapper.listStatusCount(dto.getPermissionSql());
        //全部
        int allCount = countList.stream().mapToInt(DmpPullTaskDTO.TabListDTO::getCount).sum();
        DmpPullTaskDTO.TabListDTO all = new DmpPullTaskDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(DmpConstant.ALL);
        result.add(all);

        //同步成功
        DmpPullTaskDTO.TabListDTO success = new DmpPullTaskDTO.TabListDTO();
        success.setTabFlag(SyncStatusEnum.SUCCESS_SYNC.getCode());
        int successCount = countList.stream().filter(a -> a.getTabFlag().equals(success.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        success.setCount(successCount);
        result.add(success);

        //同步失败
        DmpPullTaskDTO.TabListDTO failed = new DmpPullTaskDTO.TabListDTO();
        failed.setTabFlag(SyncStatusEnum.FAILED_SYNC.getCode());
        int failedCount = countList.stream().filter(a -> a.getTabFlag().equals(failed.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        failed.setCount(failedCount);
        result.add(failed);

        //同步中
        DmpPullTaskDTO.TabListDTO syncIng = new DmpPullTaskDTO.TabListDTO();
        syncIng.setTabFlag(SyncStatusEnum.IN_SYNC.getCode());
        int syncIngCount = countList.stream().filter(a -> a.getTabFlag().equals(syncIng.getTabFlag())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        syncIng.setCount(syncIngCount);
        result.add(syncIng);
        return result;
    }

    @Override
    public PagingVO<DmpPullTaskDTO.ListDTO> paging(PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        DmpPullTaskDTO.ParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<DmpPullTaskDTO.ListDTO> records = pageData.getRecords();
        //数据处理
        doOpHandleDmpPushTask(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(DmpPullTaskDTO.ParamDTO dto, HttpServletResponse response) {
        List<DmpPullTaskDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //数据处理
        doOpHandleDmpPushTask(list);
        List<DmpPullTaskExportExcelDTO> resultList = BeanMapperUtils.copyList(DmpPullTaskExportExcelDTO.class, list);
        String fileName = "中台拉取任务表";
        try {
            ExcelUtil.export(fileName, "中台拉取任务表", resultList, DmpPullTaskExportExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchSync(List<String> ids) {
        List<DmpPullTaskEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        for (DmpPullTaskEntity dmpPullTaskEntity : list) {
            // 发送MQ消息
            DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpPullTaskEntity.getId(), dmpPullTaskEntity.getMqData());
            SendResult result = mqProducerService.syncClassMsg(dmpPullTaskEntity.getMqTopic(), dmpPullTaskEntity.getMqTag(), dmpSyncMqDTO, dmpPullTaskEntity.getSourceId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }
        return Boolean.TRUE;
    }

    /**
     * @description: 列表查询数据格式话
     * @author Will
     * @date: 2023/10/13 15:05
     * @param list
     */
    private void doOpHandleDmpPushTask (List<DmpPullTaskDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DmpPullTaskDTO.ListDTO listDTO : list) {
            listDTO.setSyncTypeName("拉取");
            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //同步状态名称
            listDTO.setStatusName(SyncStatusEnum.getNameByCode(listDTO.getStatus()));
        }
    }


    @Override
    public String savePullTask(DmpPullTaskFeignDTO dto) {
        // 保存任务表
        try {
            DmpPullTaskEntity entity = new DmpPullTaskEntity(dto.getTargetPlatformName(),
                    dto.getMqTopic(), dto.getMqTag(), dto.getMqData(), SyncStatusEnum.IN_SYNC.getCode(),
                    dto.getSourcePlatformName(), dto.getSourceType(), dto.getSourceId(), dto.getSourceCode(), 0);
            this.saveOrUpdateDmpSyncTask(entity);
            return entity.getId();
        } catch (Exception e) {
            log.error("savePullTask 保存数据异常，{}", e.getMessage());
        }
        return null;
    }


    @Override
    public Boolean sendMqAndSaveTask(DmpPullTaskFeignDTO dto) {
        // 保存任务表
        try {
            DmpPullTaskEntity entity = new DmpPullTaskEntity(dto.getTargetPlatformName(),
                    dto.getMqTopic(), dto.getMqTag(), dto.getMqData(), SyncStatusEnum.IN_SYNC.getCode(),
                    dto.getSourcePlatformName(), dto.getSourceType(), dto.getSourceId(), dto.getSourceCode(), 0);
            this.saveOrUpdateDmpSyncTask(entity);
            // 发送MQ消息
            DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(entity.getId(), dto.getMqData());
            SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), dmpSyncMqDTO, entity.getSourceId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
                log.error("发送MQ数据异常，{}", JSONUtil.toJsonStr(result));
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("sendMqAndSaveTask 发送MQ数据异常，{}", e.getMessage());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
