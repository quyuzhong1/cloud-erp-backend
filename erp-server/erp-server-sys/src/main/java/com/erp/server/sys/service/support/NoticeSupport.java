package com.erp.server.sys.service.support;

import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.model.sys.enums.ReleaseTypeEnum;
import com.erp.model.sys.enums.SysTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

public final class NoticeSupport {

    private NoticeSupport() {
    }

    public static boolean isRealtimeSystemNotice(MessageEntity messageEntity) {
        if (messageEntity == null) {
            return false;
        }
        if (!Objects.equals(MessageTypeEnum.SYS.getCode(), messageEntity.getType())) {
            return false;
        }
        return Objects.equals(SysTypeEnum.PDA.getCode(), messageEntity.getApplication())
                || Objects.equals(SysTypeEnum.PC.getCode(), messageEntity.getApplication());
    }

    public static boolean isPdaUpgradeNotice(MessageEntity messageEntity) {
        if (messageEntity == null) {
            return false;
        }
        return Objects.equals(MessageTypeEnum.PDA.getCode(), messageEntity.getType());
    }

    public static LocalDateTime resolveExecuteTime(MessageEntity messageEntity) {
        if (messageEntity != null
                && messageEntity.getNoticeTime() != null
                && messageEntity.getNoticeTime().isAfter(LocalDateTime.now())) {
            return messageEntity.getNoticeTime();
        }
        return LocalDateTime.now();
    }

    public static MessageDTO.NoticeDTO buildSystemNotice(MessageEntity messageEntity) {
        MessageDTO.NoticeDTO dto = new MessageDTO.NoticeDTO();
        dto.setId(messageEntity.getId());
        dto.setReleaseType(ReleaseTypeEnum.PDA_SYSTEM.getCode());
        dto.setMessageType(messageEntity.getType());
        dto.setApplication(messageEntity.getApplication());
        dto.setNoticeTitle(messageEntity.getNoticeTitle());
        dto.setContent(StringUtils.defaultIfBlank(messageEntity.getDataJson(), messageEntity.getRemark()));
        dto.setNoticeTime(messageEntity.getNoticeTime());
        dto.setExpireTime(messageEntity.getExpireTime());
        return dto;
    }

    public static MessageDTO.NoticeDTO buildUpgradeNotice(MessageEntity messageEntity) {
        MessageDTO.NoticeDTO dto = new MessageDTO.NoticeDTO();
        dto.setId(messageEntity.getId());
        dto.setReleaseType(ReleaseTypeEnum.PDA_UPGRADE.getCode());
        dto.setMessageType(messageEntity.getType());
        dto.setApplication(messageEntity.getApplication());
        dto.setNoticeTitle(StringUtils.defaultIfBlank(messageEntity.getNoticeTitle(), MessageTypeEnum.PDA.getName()));
        dto.setContent(StringUtils.defaultIfBlank(messageEntity.getDataJson(), messageEntity.getRemark()));
        dto.setNoticeTime(messageEntity.getNoticeTime());
        dto.setExpireTime(messageEntity.getExpireTime());
        return dto;
    }
}
