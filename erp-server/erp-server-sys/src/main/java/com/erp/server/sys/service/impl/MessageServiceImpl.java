package com.erp.server.sys.service.impl;

import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
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
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
            MessageDTO.NotReadMessageNum notReadMessageNum = new MessageDTO.NotReadMessageNum();
            notReadMessageNum.setType(typeEnum.getCode());
            notReadMessageNum.setTypeName(typeEnum.getName());
            notReadMessageNum.setTypeRemark(typeEnum.getRemark());
            notReadMessageNum.setCount(list.size() - messageUserReadEntities.size());
            List<MessageEntity> messageEntities = list.stream().filter(req -> req.getCode().equals(typeEnum.getCode())).collect(Collectors.toList());
            notReadMessageNum.setLatestTime(messageEntities.get(MathUtil.ZERO).getCode());
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
        List<MessageEntity> list = this.list();
        for (MessageEntity messageEntity : list) {
            if (!messageIds.contains(messageEntity.getId())) {
                MessageDTO.NotReadMessageNumDetail notReadMessageNumDetail = new MessageDTO.NotReadMessageNumDetail();
                notReadMessageNumDetail.setId(messageEntity.getId());
                notReadMessageNumDetail.setDataJson(messageEntity.getDataJson());
                notReadMessageNumDetail.setIsRead(Boolean.FALSE);
                notReadMessageNumDetailList.add(notReadMessageNumDetail);
            }
        }
        return notReadMessageNumDetailList;
    }
}
