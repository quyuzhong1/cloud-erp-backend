package com.common.business.dto.base;

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
}
