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
     * 单据编号
     */
    private String code;
    /**
     * 处理结果
     */
    private String msg;

    public BatchResultDTO(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public static BatchResultDTO success(String code, OperationTypeEnum operationType) {
        if (null == operationType) {
            return new BatchResultDTO(code, "");
        }
        code = null == code ? "" : code;
        return new BatchResultDTO(code, StrUtil.format("{}_{}",operationType.getName(),"成功"));
    }

    public static BatchResultDTO success(String code, String msg) {
        code = null == code ? "" : code;
        return new BatchResultDTO(code, msg);
    }

    public static BatchResultDTO fail(String code, OperationTypeEnum operationType) {
        if (null == operationType) {
            return new BatchResultDTO(code, "");
        }
        code = null == code ? "" : code;
        return new BatchResultDTO(code, StrUtil.format("{}_{}",operationType.getName(),"失败"));
    }

    public static BatchResultDTO fail(String code, String msg) {
        code = null == code ? "" : code;
        return new BatchResultDTO(code, msg);
    }
}
