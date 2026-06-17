package com.common.business.dto.base;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.FeignServiceException;
import com.common.core.exception.ServiceException;
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
        return new BatchResultDTO(id, code, CharSequenceUtil.format("{}_{}",operationType.getName(),"成功"), Boolean.TRUE);
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
        return new BatchResultDTO(id, code, CharSequenceUtil.format("{}_{}",operationType.getName(),"失败"), Boolean.FALSE);
    }

    public static BatchResultDTO fail(String id, String code, String msg) {
        code = null == code ? "" : code;
        return new BatchResultDTO(id, code, msg,Boolean.FALSE);
    }

    public static BatchResultDTO fail(String id, String code, Exception e) {
        return fail(id, code, resolveFailMsg(e));
    }

    /**
     * 解析批量失败文案：业务异常透传 msg，系统异常返回通用错误，避免暴露内部堆栈/SQL 等信息。
     */
    public static String resolveFailMsg(Exception e) {
        if (e instanceof ServiceException) {
            return ((ServiceException) e).getMsg();
        }
        if (e instanceof FeignServiceException) {
            return ((FeignServiceException) e).getMsg();
        }
        return ApiError.HTTP_UNKNOWN.getMsg();
    }
}
