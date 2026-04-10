package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.AssetNoticeDTO;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.entity.AssetNoticeDetailEntity;
import com.erp.model.scm.entity.AssetNoticeEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.dto.SysVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.model.sys.enums.NoticeTimeTypeEnum;
import com.erp.model.sys.enums.SysTypeEnum;
import com.erp.model.sys.utils.RedisKeyUtil;
import com.erp.server.sys.mapper.MessageMapper;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import com.erp.server.sys.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息通知表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
 */
@Slf4j
@Service
public class MessageServiceImpl extends SuperServiceImpl<MessageMapper, MessageEntity> implements MessageService {
    @Resource
    private MessageUserReadService messageUserReadService;

    @Resource
    private RedisService redisService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<MessageDTO.NotReadMessageNum> listNotReadMessageNum() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //获取所有消息通知
        MessageDTO.PdaParamDTO paramDTO = new MessageDTO.PdaParamDTO();
        paramDTO.setUserId(userInfo.getUid());
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode()));
        List<MessageDTO.NotReadMessageNum> notReadMessageNumList = baseMapper.listNotReadMessageNum(paramDTO);
        notReadMessageNumList.forEach(n -> {
        	MessageTypeEnum messageTypeEnum = MessageTypeEnum.getMessageTypeEnum(n.getType());
        	if(messageTypeEnum != null) {
        		n.setTypeName(messageTypeEnum.getName());
        		n.setTypeRemark(messageTypeEnum.getRemark());
        	}
        });
        return notReadMessageNumList;
    }

    @Override
    public List<MessageDTO.NotReadMessageNumDetail> listNotReadMessageDetail(String type , Integer pageNo , Integer pageSize) {
        List<MessageDTO.NotReadMessageNumDetail> notReadMessageNumDetailList = new ArrayList<>();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //获取所有消息通知
        MessageDTO.PdaParamDTO paramDTO = new MessageDTO.PdaParamDTO();
        paramDTO.setType(type);
        paramDTO.setUserId(userInfo.getUid());
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode(), SysTypeEnum.PC.getCode()));
        if(pageNo == null) {
        	pageNo = 1;
        }
        if(pageSize == null) {
        	pageSize = 1000;
        }
        paramDTO.setPageSize(pageSize);
        paramDTO.setOffSet((pageNo - 1) * pageSize);
        List<MessageEntity> list = baseMapper.list(paramDTO);
        for (MessageEntity messageEntity : list) {
            MessageDTO.NotReadMessageNumDetail notReadMessageNumDetail = new MessageDTO.NotReadMessageNumDetail();
            notReadMessageNumDetail.setId(messageEntity.getId());
            notReadMessageNumDetail.setDataJson(messageEntity.getDataJson());
            notReadMessageNumDetail.setCreateTime(messageEntity.getCreateTime());
            notReadMessageNumDetail.setIsRead(messageEntity.getIsRead());
            notReadMessageNumDetailList.add(notReadMessageNumDetail);
        }
        newReadMessage(type);
        notReadMessageNumDetailList.sort(Comparator.comparing(MessageDTO.NotReadMessageNumDetail::getCreateTime));
        return notReadMessageNumDetailList;
    }

    @Override
    public Boolean readAll() {
        newReadMessage(null);
        return Boolean.TRUE;
    }
    
    private void newReadMessage(String type) {
    	LoginUser userInfo = UserContext.getDefaultLoginUser();
    	
    	String lastSql = " and message_id in (select id from message where application IN ( 'PDA' , 'ALL' ) ) ";
    	if(StringUtils.isNotBlank(type)) {
    		lastSql = " and message_id in (select id from message where application IN ( 'PDA' , 'ALL' ) and type = '"+ type +"' ) ";
    	}
    	
    	messageUserReadService.lambdaUpdate().set(MessageUserReadEntity::getIsRead, true)
	    	.eq(MessageUserReadEntity::getIsRead, false)
	    	.eq(MessageUserReadEntity::getUserId, userInfo.getUid())
	    	.last(lastSql).update();
    	
        redisService.deleteObject(RedisKeyUtil.getCloseMessageNoticeKey(userInfo.getUid()));
    }

    @Override
    public List<MessageEntity> listByType(String type) {
        if (StringUtils.isBlank(type)) {
            return new ArrayList<>();
        }
        return lambdaQuery().eq(MessageEntity::getType, type).list();
    }

    /**
     * 读取消息
     * @Author Luo_WG
     * @Date 2023/8/11 16:34
     * @param messageUserReadEntities 已读的消息
     * @param messageEntityList 需要读取的消息
     * @return void
     **/
    private void readMessage(List<MessageUserReadEntity> messageUserReadEntities, List<MessageEntity> messageEntityList) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<String> messageIds = messageUserReadEntities.stream().map(req -> req.getMessageId()).collect(Collectors.toList());
        for (MessageEntity messageEntity : messageEntityList) {
            //读取未读消息
            if (!messageIds.contains(messageEntity.getId())) {
                messageUserReadService.readByMessageId(messageEntity.getId(), userInfo.getUid());
            }
        }
        redisService.deleteObject(RedisKeyUtil.getCloseMessageNoticeKey(userInfo.getUid()));
    }

    @Override
    public MessageDTO.IsMessageDTO isMessage() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        String uid = userInfo.getUid();
        MessageDTO.PdaParamDTO paramDTO = new MessageDTO.PdaParamDTO();
        paramDTO.setUserId(userInfo.getUid());
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode()));

        List<MessageEntity> messageEntities = baseMapper.listByNotReadMessage(paramDTO);

        List<String> typeList = messageEntities.stream().map(req -> MessageTypeEnum.getName(req.getType())).distinct().collect(Collectors.toList());

        MessageDTO.IsMessageDTO isMessageDTO = new MessageDTO.IsMessageDTO();

        if (CollectionUtils.isNotEmpty(messageEntities)) {
            Integer cacheObject = redisService.getCacheObject(RedisKeyUtil.getCloseMessageNoticeKey(uid));
            //表示有叉掉过消息通知
            if (cacheObject != null) {
                if (cacheObject  >= messageEntities.size()) {
                    return isMessageDTO;
                }
            }
            isMessageDTO.setRemark(StrUtil.format("有{}条新的{}",messageEntities.size(), StringUtils.join(typeList, "/")));
        }
        return isMessageDTO;
    }

    @Override
    public Boolean closeMessageNotice() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        MessageDTO.PdaParamDTO paramDTO = new MessageDTO.PdaParamDTO();
        paramDTO.setUserId(userInfo.getUid());
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode()));

        List<MessageEntity> messageEntities = baseMapper.listByNotReadMessage(paramDTO);
        redisService.setCacheObject(RedisKeyUtil.getCloseMessageNoticeKey(userInfo.getUid()), messageEntities.size(), RedisCacheConstants.EXPIRATION, TimeUnit.DAYS);
        return Boolean.TRUE;
    }

    @Override
    public BaseResultDTO.AddDTO add(MessageDTO.AddDTO addDTO) {
        MessageEntity messageEntity = new MessageEntity();
        BeanMapperUtils.copy(addDTO, messageEntity);
        messageEntity.setApplication(MessageTypeEnum.SYS.getCode());
        //这两个字段要注意,当初设计的时候就是这样对应的
        messageEntity.setType(addDTO.getReleaseType());
        messageEntity.setApplication(addDTO.getType());
        // 数据处理
        handleData(messageEntity);

        log.info("开始新增系统通知");
        boolean save = super.save(messageEntity);
        if(!save) {
            throw new ServiceException("系统通知保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】标题为【{}】", UserContext.getDefaultLoginUser().getUserName(), "系统通知" , messageEntity.getNoticeTitle());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.MESSAGE.getCode(), messageEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(messageEntity.getId(), "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(MessageDTO.UpdateDTO addOrUpdateDTO) {
        MessageEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "系统通知"));
        MessageEntity messageEntity = BeanMapperUtils.map(MessageEntity.class, addOrUpdateDTO);
        //这两个字段要注意,当初设计的时候就是这样对应的
        messageEntity.setType(addOrUpdateDTO.getReleaseType());
        messageEntity.setApplication(addOrUpdateDTO.getType());
        // 数据处理
        handleData(messageEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(messageEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，id：【{}】", messageEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(),  "系统通知");
        operateLogService.addModuleOperateLogByObj(old, messageEntity, ModuleTypeEnum.MESSAGE.getCode(), messageEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    @Override
    public MessageDTO.ViewDTO view(String id) {
        MessageEntity messageEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到数据"));
        MessageDTO.ViewDTO data = BeanMapperUtils.map(MessageDTO.ViewDTO.class, messageEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    @Override
    public BatchResultDTO deleteMessage(String id) {
        MessageEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));

        log.info("删除 开始删除数据，id：【{}】", id);
        super.removeById(id);

        messageUserReadService.removeByMessageId(id);

        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "", "系统公告");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.MESSAGE.getCode(), entity.getId(), "删除");
        return BatchResultDTO.success(entity.getId(), "", OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO deleteVersion(String id) {
        MessageEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));

        log.info("删除 开始删除数据，id：【{}】", id);
        super.removeById(id);

        messageUserReadService.removeByMessageId(id);

        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "", "版本更新");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SYS_VERSION.getCode(), entity.getId(), "删除");
        return BatchResultDTO.success(entity.getId(), "", OperationTypeEnum.DELETE);
    }

    @Override
    public BaseResultDTO.AddDTO addSysVersion(SysVersionDTO.AddDTO addDTO) {
        MessageEntity messageEntity = new MessageEntity();
        BeanMapperUtils.copy(addDTO, messageEntity);
        messageEntity.setApplication(MessageTypeEnum.SYS_VERSION.getCode());
        // 数据处理
        handleData(messageEntity);

        log.info("开始新增系统通知");
        boolean save = super.save(messageEntity);
        if(!save) {
            throw new ServiceException("系统通知保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】标题为【{}】", UserContext.getDefaultLoginUser().getUserName(), "版本更新" , messageEntity.getNoticeTitle());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SYS_VERSION.getCode(), messageEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(messageEntity.getId(), "");
    }

    @Override
    public PagingVO<SysVersionDTO.ListDTO> pagingSysVersion(PagingDTO<SysVersionDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SysVersionDTO.ListDTO> pageData = this.baseMapper.pagingSysVersion(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<MessageDTO.ListHistoryMessageDTO> pagingHistoryMessage(PagingDTO<MessageDTO.HistoryMessagePagingParamDTO> dto) {
        return null;
    }

    @Override
    public boolean readHistoryMessage(MessageDTO.ReadHistoryMessageDTO dto) {
        return false;
    }

    @Override
    public PagingVO<SysVersionDTO.ListHistoryVersionDTO> pagingHistoryVersion(PagingDTO<SysVersionDTO.HistoryVersionPagingParamDTO> dto) {
        return null;
    }

    @Override
    public boolean readHistoryVersion(SysVersionDTO.ReadHistoryVersionDTO dto) {
        return false;
    }

    @Override
    public SysVersionDTO.LatestVersionDTO getLatestVersion() {
        return null;
    }

    private void handleData(MessageEntity messageEntity) {
        if (Objects.nonNull(messageEntity.getNoticeTime())
                && messageEntity.getNoticeTime().isAfter(LocalDateTime.now())) {
            throw new ServiceException(ApiError.COMMON_NOTICE_TIME_AFTER_NOW);
        }
    }

    public void fillOne(MessageDTO.ViewDTO data){
        data.setTypeName(MessageTypeEnum.getName(data.getType()));
        data.setReleaseTypeName(NoticeTimeTypeEnum.getName(data.getType()));
    }

    public void fillList(List<SysVersionDTO.ListDTO> records){

    }

}
