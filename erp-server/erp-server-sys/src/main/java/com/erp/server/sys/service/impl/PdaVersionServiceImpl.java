package com.erp.server.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.entity.PdaUserSkipVersionEntity;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.model.sys.enums.SysTypeEnum;
import com.erp.server.sys.mapper.PdaVersionMapper;
import com.erp.server.sys.service.*;
import com.erp.server.sys.service.support.NoticeSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
 */
@Slf4j
@Service
public class PdaVersionServiceImpl extends SuperServiceImpl<PdaVersionMapper, PdaVersionEntity> implements PdaVersionService {
    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private MessageService messageService;

    @Resource
    private MessageUserReadService messageUserReadService;

    @Resource
    private PdaUserSkipVersionService pdaUserSkipVersionService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private MessageDispatchTaskService messageDispatchTaskService;

    @Override
    public PagingVO<PdaVersionDTO.PagingDTO> paging(PagingDTO<PdaVersionDTO.PagingParamDTO> dto) {
        PdaVersionDTO.PagingParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<PdaVersionDTO.PagingDTO> pageData = baseMapper.paging(query, params);
        List<PdaVersionDTO.PagingDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        list.forEach(req -> req.setTypeName(SysTypeEnum.getName(req.getType())));
        return new PagingVO(pageData);
    }

    @Override
    public PdaVersionEntity getPdaVersion() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return baseMapper.getPdaVersion(userInfo.getUid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean release(PdaVersionDTO.AddDTO dto){
        try{

            PdaVersionEntity entity = new PdaVersionEntity();
            BeanMapper.copy(dto, entity);
            boolean flag = this.save(entity);
            if (flag) {
                List<FindUserDTO> allUserList = sysUserInfoService.getAllUserList(1);
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.setType(MessageTypeEnum.PDA.getCode());
                messageEntity.setRemark(dto.getRemark());
                messageEntity.setDataJson(dto.getRemark());
                messageEntity.setNoticeTitle(MessageTypeEnum.PDA.getName());
                messageEntity.setNoticeTime(dto.getUpgradeTime());
                messageEntity.setApplication(dto.getType());
                boolean messageSaved = messageService.save(messageEntity);
                if (!messageSaved) {
                    throw new ServiceException("PDA升级通知消息保存失败");
                }
                List<MessageUserReadEntity> readEntityList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(allUserList)) {
                    for (FindUserDTO findUserDTO : allUserList) {
                        MessageUserReadEntity readEntity = new MessageUserReadEntity();
                        readEntity.setMessageId(messageEntity.getId());
                        readEntity.setUserId(findUserDTO.getUserId());
                        readEntity.setIsRead(Boolean.FALSE);
                        readEntityList.add(readEntity);
                    }
                }
                if (CollectionUtils.isNotEmpty(readEntityList)) {
                    messageUserReadService.saveBatch(readEntityList);
                }
                String taskId = messageDispatchTaskService.createUpgradePushTask(messageEntity);
                LocalDateTime executeTime = NoticeSupport.resolveExecuteTime(messageEntity);
                runAfterCommit(() -> messageDispatchTaskService.queueTask(taskId, executeTime));
            }
            return flag;
        }catch (Exception e){
            throw new ServiceException("PDA升级通知发版失败",e);
        }
    }

    @Override
    public Boolean skipVersion(String versionId) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        PdaUserSkipVersionEntity entity = new PdaUserSkipVersionEntity();
        entity.setUserId(userInfo.getUid());
        entity.setUserName(userInfo.getUserName());
        entity.setVersionId(versionId);
        return pdaUserSkipVersionService.save(entity);
    }

    @Override
    public Boolean update(PdaVersionDTO.UpdateDTO dto) {
        PdaVersionEntity old = super.getById(dto.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "PDA升级通知"));
        PdaVersionEntity pdaVersionEntity = BeanMapperUtils.map(PdaVersionEntity.class, dto);
        // 数据处理
        handleData(pdaVersionEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(pdaVersionEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，id：【{}】", pdaVersionEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(),  "PDA升级通知");
        operateLogService.addModuleOperateLogByObj(old, pdaVersionEntity, ModuleTypeEnum.PDA_VERSION.getCode(), pdaVersionEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        PdaVersionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));

        log.info("删除 开始删除数据，id：【{}】", id);
        this.removeById(id);
        messageUserReadService.removeByMessageId(id);

        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "", "系统公告");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PDA_VERSION.getCode(), entity.getId(), "删除");
        return BatchResultDTO.success(entity.getId(), "", OperationTypeEnum.DELETE);
    }

    @Override
    public PdaVersionDTO.ViewDTO view(String id) {
        PdaVersionEntity pdaVersionEntity = this.getById(id);
        if (pdaVersionEntity == null) {
            throw new ServiceException("未找到数据");
        }
        PdaVersionDTO.ViewDTO data = BeanMapperUtils.map(PdaVersionDTO.ViewDTO.class, pdaVersionEntity);
        return data;
    }

    private void handleData(PdaVersionEntity entity) {
        if (Objects.nonNull(entity.getUpgradeTime())
                && entity.getUpgradeTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ApiError.COMMON_NOTICE_TIME_AFTER_NOW);
        }
    }

    private void runAfterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runnable.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
