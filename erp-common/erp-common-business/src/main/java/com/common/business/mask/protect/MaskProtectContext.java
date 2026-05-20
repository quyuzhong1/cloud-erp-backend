package com.common.business.mask.protect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Redis 中保存的脱敏回显保护上下文。
 *
 * @author cloud-erp
 */
@Data
public class MaskProtectContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private String userId;
    private String classPath;
    private String fieldName;
    private List<MaskProtectBinding> paramBindings = new ArrayList<>();
    private String recordId;
    private String versionFieldName;
    private String versionValue;
    private MaskProtectVerifyMode verifyMode;
    private String tableName;
    private String recordIdColumn;
    private String valueColumn;
    private String deletedColumn;
    private String permissionCode;
    private String originalValue;
    private Boolean originalNull;
    private Long createTimeMillis;
    private Long expireTimeMillis;
    private String nonce;
}
