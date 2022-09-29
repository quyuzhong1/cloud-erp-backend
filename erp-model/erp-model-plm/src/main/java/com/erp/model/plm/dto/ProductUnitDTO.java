package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName product_unit
 */
@Data
public class ProductUnitDTO implements Serializable {

    @ApiModelProperty(value = "主键id 无id：修改 有id：新增")
    private String id;

    @ApiModelProperty(value = "单位名称")
    private String name;

    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "修改人id")
    private String updateUserId;

    @ApiModelProperty(value = "修改人名称")
    private String updateUserName;

    private static final long serialVersionUID = 1L;
}