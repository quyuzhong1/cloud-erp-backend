package com.erp.server.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.model.sys.enums.SysTypeEnum;
import com.erp.model.sys.utils.RedisKeyUtil;
import com.erp.server.sys.mapper.MessageMapper;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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

    @Override
    public List<MessageDTO.NotReadMessageNum> listNotReadMessageNum() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //获取所有消息通知
        MessageDTO.PdaParamDTO paramDTO = new MessageDTO.PdaParamDTO();
        paramDTO.setUserId(userInfo.getUid());
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode(), SysTypeEnum.ALL.getCode()));
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
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode(), SysTypeEnum.ALL.getCode()));
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
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode(), SysTypeEnum.ALL.getCode()));

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
        paramDTO.setApplication(Arrays.asList(SysTypeEnum.PDA.getCode(), SysTypeEnum.ALL.getCode()));

        List<MessageEntity> messageEntities = baseMapper.listByNotReadMessage(paramDTO);
        redisService.setCacheObject(RedisKeyUtil.getCloseMessageNoticeKey(userInfo.getUid()), messageEntities.size(), RedisCacheConstants.EXPIRATION, TimeUnit.DAYS);
        return Boolean.TRUE;
    }

}
