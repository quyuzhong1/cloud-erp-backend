package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpPushTaskExportExcelDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMqAndSaveTask(DmpPushTaskFeignDTO dto) {
        // 保存任务表
        DmpPushTaskEntity entity = new DmpPushTaskEntity(dto);
        String entityId = saveOrUpdateDmpSyncTask(entity);
        // 发送MQ消息
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(entityId, dto.getMqData());
        SendResult result = mqProducerService.syncClassMsg(dto.getMqTopic(), dto.getMqTag(), dmpSyncMqDTO, entity.getSourceId());
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
        for (DmpPushTaskEntity dmpPushTaskEntity : list) {
            // 发送MQ消息
            DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpPushTaskEntity.getId(), dmpPushTaskEntity.getMqData());
            SendResult result = mqProducerService.syncClassMsg(dmpPushTaskEntity.getMqTopic(), dmpPushTaskEntity.getMqTag(), dmpSyncMqDTO, dmpPushTaskEntity.getSourceId());
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
    private void doOpHandleDmpPushTask (List<DmpPushTaskDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DmpPushTaskDTO.ListDTO listDTO : list) {
            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //同步状态名称
            listDTO.setStatusName(SyncStatusEnum.getNameByCode(listDTO.getStatus()));
            //同步操作名称
            listDTO.setSyncOperateName(SyncStatusEnum.getNameByCode(listDTO.getSyncOperate()));
        }
    }

    /**
     * 新增或修改
     */
    private String saveOrUpdateDmpSyncTask(DmpPushTaskEntity entity) {
        DmpSyncTaskDTO.OneDTO map = BeanMapperUtils.map(DmpSyncTaskDTO.OneDTO.class, entity);
        DmpPushTaskEntity found = getByParam(map);
        //存在则修改
        if (ObjectUtil.isNotEmpty(found)) {
            entity.setId(found.getId());
        }
        this.saveOrUpdate(entity);
        return entity.getId();
    }
}
