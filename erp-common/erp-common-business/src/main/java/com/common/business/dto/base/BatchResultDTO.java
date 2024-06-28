package com.common.business.dto.base;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 批量处理结果
 *
 * @Author Cloud
 * @Date 2023/8/11 11:05
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResultDTO implements Serializable {
    

    /**
     * 单据id
     */
    private String id;
    /**
     * 单据编号
     */
    private String code;
    /**
     * 处理结果
     */
    private String msg;

    private Boolean success;

    public static BatchResultDTO success(String id, String code, OperationTypeEnum operationType) {
        if (null == operationType) {
            return new BatchResultDTO(id, code, "", Boolean.TRUE);
        }
        code = null == code ? "" : code;
        return new BatchResultDTO(id, code, StrUtil.format("{}_{}",operationType.getName(),"成功"), Boolean.TRUE);
    }

    public static BatchResultDTO success(String id, String code) {
        code = null == code ? "" : code;
        return new BatchResultDTO(id, code, "成功", Boolean.TRUE);
    }
    public static BatchResultDTO success() {
        return new BatchResultDTO("","","",Boolean.TRUE);
    }
    public static BatchResultDTO success(String id, String code, String msg) {
        code = null == code ? "" : code;
        return new BatchResultDTO(id, code, msg, Boolean.TRUE);
    }

    public static BatchResultDTO fail(String id, String code, OperationTypeEnum operationType) {
        if (null == operationType) {
            return new BatchResultDTO(id, code, "", Boolean.FALSE);
        }
        code = null == code ? "" : code;
        return new BatchResultDTO(id, code, StrUtil.format("{}_{}",operationType.getName(),"失败"), Boolean.FALSE);
    }

    public static BatchResultDTO fail(String id, String code, String msg) {
        code = null == code ? "" : code;
        return new BatchResultDTO(id, code, msg,Boolean.FALSE);
    }
}
