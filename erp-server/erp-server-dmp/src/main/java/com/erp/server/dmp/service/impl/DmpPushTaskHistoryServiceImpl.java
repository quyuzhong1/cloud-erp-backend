package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpPushTaskExportExcelDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskHistoryEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.mapper.DmpPushTaskHistoryMapper;
import com.erp.server.dmp.service.DmpPushTaskHistoryService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
public class DmpPushTaskHistoryServiceImpl extends ServiceImpl<DmpPushTaskHistoryMapper, DmpPushTaskHistoryEntity> implements DmpPushTaskHistoryService {

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;

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
        List<DmpPushTaskExportExcelDTO> resultList = list.stream().map(entity -> {
            DmpPushTaskExportExcelDTO excelDTO = new DmpPushTaskExportExcelDTO();
            BeanUtils.copyProperties(entity, excelDTO);
            return excelDTO;
        }).collect(Collectors.toList());
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
        List<DmpPushTaskHistoryEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        //需要修改备注信息
        for (DmpPushTaskHistoryEntity dmpPushTaskEntity : list) {
            try {
                //查询来源上级单据
                Boolean isSend = isSendParentBillTask(dmpPushTaskEntity);
                DmpPushTaskEntity dmpPushTask = new DmpPushTaskEntity();
                BeanUtils.copyProperties(dmpPushTaskEntity, dmpPushTask);
                dmpPushTaskService.save(dmpPushTask);
                baseMapper.deleteById(dmpPushTaskEntity.getId());
                //判断是否存在上级单据，并且推送成功
                if (Boolean.FALSE.equals(isSend)) {
                    continue;
                }
                sendMq(dmpPushTaskEntity.getMqData(), dmpPushTaskEntity.getId(), mqProducerService, dmpPushTaskEntity.getMqTopic(), dmpPushTaskEntity.getMqTag(), dmpPushTaskEntity.getSourceId());
            } catch (Exception e) {
                String sourceTypeName = SourceTypeEnum.getName(dmpPushTaskEntity.getSourceType());
                log.error("从{}推送{}到{}发送消息异常", dmpPushTaskEntity.getSourcePlatformName(), sourceTypeName, dmpPushTaskEntity.getTargetPlatformName(), e);
            }
        }
        return Boolean.TRUE;
    }

    public static void sendMq(String mqData2, String id, MQProducerService mqProducerService, String mqTopic, String mqTag, String sourceId) {
        String mqData = mqData2;
        JSONObject jsonObject = JSONUtil.parseObj(mqData);
        jsonObject.set("dmpSyncTaskId", id);
        SendResult result = mqProducerService.syncClassMsg(mqTopic, mqTag, JSONUtil.toJsonStr(jsonObject), sourceId);
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchFindDataSync(List<String> ids) {
        List<DmpPushTaskHistoryEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_DMP_PUSH_TASK);
        }
        long count = list.stream().filter(obj -> !PlatformEnum.ERP.getDesc().equals(obj.getSourcePlatformName()) || !PlatformEnum.KINGDEE.getDesc().equals(obj.getTargetPlatformName())).count();
        if (count > 0) {
            throw new ServiceException(new ApiResult(10000, "只允许推送自研ERP>>>>金蝶的数据"));
        }
        List<DmpPushTaskEntity> entities = list.stream().map(entity -> {
            DmpPushTaskEntity e = new DmpPushTaskEntity();
            BeanUtils.copyProperties(entity, e);
            return e;
        }).collect(Collectors.toList());
        dmpPushTaskService.saveBatch(entities);
        baseMapper.deleteBatchIds(ids);
        Map<String, List<DmpPushTaskHistoryEntity>> map = list.stream().collect(Collectors.groupingBy(DmpPushTaskHistoryEntity::getSourceType));
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                for (Map.Entry<String, List<DmpPushTaskHistoryEntity>> entry : map.entrySet()) {
                    String sourceType = entry.getKey();
                    List<DmpPushTaskHistoryEntity> value = entry.getValue();
                    List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList = value.stream().map(obj -> new DmpSyncMqDTO.SyncParamDetailDTO(obj.getSourceId(), obj.getSyncOperate())).collect(Collectors.toList());
                    try {
                        // 发送MQ消息
                        findDataAndSendMq(paramDetailList, sourceType);
                    } catch (Exception e) {
                        String sourceTypeName = SourceTypeEnum.getName(sourceType);
                        log.error("从{}推送{}到{}发送消息异常", PlatformEnum.ERP.getDesc(), sourceTypeName, PlatformEnum.KINGDEE.getDesc(), e);
                    }
                }
            }
        });

        return Boolean.TRUE;
    }


    public Boolean isSendParentBillTask(DmpPushTaskHistoryEntity entity) {
        if (StrUtil.isBlank(entity.getParentId())) {
            return Boolean.TRUE;
        }
        List<String> parentIdList = Arrays.stream(entity.getParentId().split(",")).collect(Collectors.toList());
        DmpPushTaskHistoryEntity dmpPushTaskEntity = this.getOne(Wrappers.<DmpPushTaskHistoryEntity>lambdaQuery().in(DmpPushTaskHistoryEntity::getSourceId, parentIdList).last("limit 1"));
        DmpPushTaskEntity dmpPushTask = dmpPushTaskService.getOne(Wrappers.<DmpPushTaskEntity>lambdaQuery().in(DmpPushTaskEntity::getSourceId, parentIdList).last("limit 1"));

        if (ObjectUtil.isEmpty(dmpPushTaskEntity) && ObjectUtil.isEmpty(dmpPushTask)) {
            entity.setReturnMsg("未找到上级单据推送任务");
            entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            return Boolean.FALSE;
        }
        if (!SyncStatusEnum.SUCCESS_SYNC.getCode().equals(dmpPushTaskEntity.getStatus()) || !SyncStatusEnum.SUCCESS_SYNC.getCode().equals(dmpPushTask.getStatus())) {
            entity.setReturnMsg("上级单据未推送成功，不支持推送下级单据");
            entity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * @description: 重新查询数据发送MQ
     * @author Will
     * @date: 2023/10/30 10:03
     */
    private void findDataAndSendMq(List<DmpSyncMqDTO.SyncParamDetailDTO> paramDetailList, String sourceType) {
        SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(sourceType);
        DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO(paramDetailList, sourceTypeEnum);
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
            case SUBCONTRACT_ISSUE:
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
     * @param list
     * @description: 列表查询数据格式话
     * @author Will
     * @date: 2023/10/13 15:05
     */
    private void doOpHandleDmpPushTask(List<DmpPushTaskDTO.ListDTO> list) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPushTaskHistory() {
        log.info("开始归档3个月前同步成功的数据");
        //获取到今天3个月前同步成功的数据
        List<DmpPushTaskEntity> dmpPushTasks = dmpPushTaskService.list(Wrappers.<DmpPushTaskEntity>lambdaQuery()
                .lt(DmpPushTaskEntity::getCreateTime, LocalDateTime.now().minusMonths(3))
                .eq(DmpPushTaskEntity::getStatus, SyncStatusEnum.SUCCESS_SYNC.getCode())
        );
        if (CollectionUtil.isEmpty(dmpPushTasks)) {
            return;
        }
        // 分批保存防止数据量过大
        List<List<DmpPushTaskEntity>> partition = Lists.partition(dmpPushTasks, 500);
        partition.parallelStream().forEach(e -> {
            // 保存至历史表
            List<DmpPushTaskHistoryEntity> taskHistory = e.stream().map(entity ->{
                DmpPushTaskHistoryEntity history = new DmpPushTaskHistoryEntity();
                BeanUtils.copyProperties(entity, history);
                history.setId(null);
                return history;
            }).collect(Collectors.toList());
            saveOrUpdateBatch(taskHistory);
            // 物理删除已经保存数据
            List<String> ids = e.stream()
                    .map(DmpPushTaskEntity::getId)
                    .collect(Collectors.toList());
            dmpPushTaskService.deleteByIds(ids);
        });
        log.info("完成归档3个月前同步成功的数据");
    }
}
