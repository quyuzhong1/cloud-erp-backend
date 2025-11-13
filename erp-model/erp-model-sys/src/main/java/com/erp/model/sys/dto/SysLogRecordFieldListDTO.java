package com.erp.model.sys.dto;

import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.EnumsUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SysLogRecordFieldListDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 字段
     */
    private String field;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 类路径
     */
    private String classPath;

    /**
     * 字段类型 0字符串，1是或否，2枚举，3字典，4人员
     */
    private Integer type;

    /**
     * 枚举类型(用于枚举值转换,需要枚举整个路径)
     */
    private String enumClass;

    /**
     * true|false对应值,竖线分隔
     */
    private String booleanValue;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 逻辑删除字段
     */
    private Boolean isDeleted;


    public static final String TRUE = "是";

    public static final String FALSE = "否";

    /**
     * 通过类型得到日志描述的内容
     */
    public String parseDescByType(Object value) {
        if (0 == type) {
            return value.toString();
        } else if (1 == type) {
            return Boolean.TRUE.toString().equals(value.toString()) ? TRUE : FALSE;
        } else if (2 == type) {
            return parseEnumValue(value);
        }
        throw new ServiceException(ApiError.ERROR_PARAM_INVALID);
    }

    /**
     * 解析枚举值
     */
    private String parseEnumValue(Object value) {
        if (StringUtils.isBlank(this.getEnumClass()) || null == value) {
            throw new ServiceException(ApiError.ERROR_ENUM_CONVERT_FAILED);
        }
        Class<?> aClass;
        try {
            aClass = Class.forName(this.getEnumClass());
        } catch (ClassNotFoundException e) {
            throw new ServiceException(ApiError.ERROR_ENUM_CONVERT_FAILED);
        }
        boolean anEnum = aClass.isEnum();
        if (!anEnum) {
            throw new ServiceException(ApiError.ERROR_ENUM_CONVERT_FAILED);
        }

        EnumMessage enumObject = EnumsUtil.getEnumObject(value, aClass);
        if (null == enumObject) {
            throw new ServiceException(ApiError.ERROR_ENUM_CONVERT_FAILED);
        }
        return enumObject.getName();
    }


}