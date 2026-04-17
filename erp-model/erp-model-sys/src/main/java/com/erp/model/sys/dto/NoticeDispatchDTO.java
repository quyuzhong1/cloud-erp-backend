package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoticeDispatchDTO implements Serializable {

    private String messageId;

    private String scene;

    private Boolean markReadOnSuccess;

    private MessageDTO.NoticeDTO notice;
}
