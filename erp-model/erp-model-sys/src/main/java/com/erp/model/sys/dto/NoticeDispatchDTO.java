package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class NoticeDispatchDTO implements Serializable {

    private String messageId;

    private String scene;

    private Boolean markReadOnSuccess;

    private Set<String> targetUserIds;

    private MessageDTO.NoticeDTO notice;
}
