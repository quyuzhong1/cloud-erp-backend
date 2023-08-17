package com.erp.server.sys.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ObjectUtils;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.server.sys.mapper.MessageMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.MessageService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.sys.service.MessageUserReadService;
import com.erp.server.sys.service.SysUserInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
    private SysUserInfoService sysUserInfoService;
    @Resource
    private MessageUserReadService messageUserReadService;

    @Resource
    private CommonService commonService;

    @Override
    public List<MessageDTO.NotReadMessageNum> listNotReadMessageNum() {
        List<MessageDTO.NotReadMessageNum> notReadMessageNumList = new ArrayList<>();
        LoginUser userInfo = commonService.getUserInfo();
        String uid = userInfo.getUid();
        //获取所有消息通知
        List<MessageEntity> list = lambdaQuery().orderByDesc(MessageEntity::getCreateTime).list();
        //获取已读的消息通知
        List<MessageUserReadEntity> messageUserReadEntities = messageUserReadService.listByUserId(uid);
        for (MessageTypeEnum typeEnum : MessageTypeEnum.values()) {
            List<MessageEntity> messageEntityList = list.stream().filter(req -> req.getType().equals(typeEnum.getCode())).collect(Collectors.toList());
            List<String> messageIds = messageEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<MessageUserReadEntity> readEntities = messageUserReadEntities.stream().filter(req -> messageIds.contains(req.getMessageId())).collect(Collectors.toList());
            MessageDTO.NotReadMessageNum notReadMessageNum = new MessageDTO.NotReadMessageNum();
            notReadMessageNum.setType(typeEnum.getCode());
            notReadMessageNum.setTypeName(typeEnum.getName());
            notReadMessageNum.setTypeRemark(typeEnum.getRemark());
            notReadMessageNum.setCount(messageEntityList.size() - readEntities.size());
            List<MessageEntity> messageEntities = list.stream().filter(req -> req.getType().equals(typeEnum.getCode())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(messageEntities)) {
                notReadMessageNum.setLatestTime(messageEntities.get(MathUtil.ZERO).getCreateTime());
            }
            notReadMessageNumList.add(notReadMessageNum);
        }
        return notReadMessageNumList;
    }

    @Override
    public List<MessageDTO.NotReadMessageNumDetail> listNotReadMessageDetail(String type) {
        List<MessageDTO.NotReadMessageNumDetail> notReadMessageNumDetailList = new ArrayList<>();
        LoginUser userInfo = commonService.getUserInfo();
        //获取已读的消息通知
        List<MessageUserReadEntity> messageUserReadEntities = messageUserReadService.listByUserId(userInfo.getUid());
        List<String> messageIds = messageUserReadEntities.stream().map(req -> req.getMessageId()).collect(Collectors.toList());
        //获取所有消息通知
        List<MessageEntity> list = this.listByType(type);
        for (MessageEntity messageEntity : list) {
            MessageDTO.NotReadMessageNumDetail notReadMessageNumDetail = new MessageDTO.NotReadMessageNumDetail();
            notReadMessageNumDetail.setId(messageEntity.getId());
            notReadMessageNumDetail.setDataJson(messageEntity.getDataJson());
            notReadMessageNumDetail.setCreateTime(messageEntity.getCreateTime());
            if (!messageIds.contains(messageEntity.getId())) {
                notReadMessageNumDetail.setIsRead(Boolean.FALSE);
            } else {
                notReadMessageNumDetail.setIsRead(Boolean.TRUE);
            }
            notReadMessageNumDetailList.add(notReadMessageNumDetail);
        }
        readMessage(messageUserReadEntities, list);
        notReadMessageNumDetailList.sort(Comparator.comparing(MessageDTO.NotReadMessageNumDetail::getCreateTime));
        return notReadMessageNumDetailList;
    }

    @Override
    public Boolean readAll() {
        LoginUser userInfo = commonService.getUserInfo();
        //获取已读的消息通知
        List<MessageUserReadEntity> messageUserReadEntities = messageUserReadService.listByUserId(userInfo.getUid());
        //获取所有消息通知
        List<MessageEntity> list = this.list();
        readMessage(messageUserReadEntities, list);
        return Boolean.TRUE;
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
        LoginUser userInfo = commonService.getUserInfo();
        List<String> messageIds = messageUserReadEntities.stream().map(req -> req.getMessageId()).collect(Collectors.toList());
        for (MessageEntity messageEntity : messageEntityList) {
            //把未读消息放到读取表中
            if (!messageIds.contains(messageEntity.getId())) {
                MessageUserReadEntity messageUserReadEntity = new MessageUserReadEntity();
                messageUserReadEntity.setMessageId(messageEntity.getId());
                messageUserReadEntity.setUserId(userInfo.getUid());
                messageUserReadService.save(messageUserReadEntity);
            }
        }
    }

    @Override
    public MessageDTO.IsMessageDTO isMessage() {
        LoginUser userInfo = commonService.getUserInfo();
        String uid = userInfo.getUid();
        List<MessageEntity> messageEntities = baseMapper.listByNotReadMessage(uid);

        List<String> typeList = messageEntities.stream().map(req -> MessageTypeEnum.getName(req.getType())).distinct().collect(Collectors.toList());

        MessageDTO.IsMessageDTO isMessageDTO = new MessageDTO.IsMessageDTO();
        if (CollectionUtils.isNotEmpty(messageEntities)) {
            isMessageDTO.setRemark(StrUtil.format("有{}条新的{}",messageEntities.size(), StringUtils.join(typeList, "/")));
        }
        return isMessageDTO;
    }

    /**
     * 查询未读消息
     * @Author Luo_WG
     * @Date 2023/8/17 14:17
     * @param userId
     * @return java.util.List<com.erp.model.sys.entity.MessageEntity>
     **/
    private List<MessageEntity> listByNotReadMessage(String userId) {
        return baseMapper.listByNotReadMessage(userId);
    }
}
