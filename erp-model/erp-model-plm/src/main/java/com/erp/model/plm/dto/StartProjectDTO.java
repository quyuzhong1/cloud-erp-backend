package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname ProjectDTO
 * @Description TODO
 * @Date 2022-09-19 10:56
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class StartProjectDTO implements Serializable {


    /**
     * 负责人id集合
     */
    @NotNull(message = "负责人id不能为空")
    @Size(min=1,message = "负责人id不能为空")
    private List<String>  chargeIdList;


    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String  productId;


    /**
     * 项目id
     */
    @NotBlank(message = "项目id不能为空")
    private String  projectId;


    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date startTime;


    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date endTime;

    //
    /**
     * 描述
     */
    private String describe;

    /**
     * 来源类型
     */
    @StateEnumValue(intValues = {0,1,2}, message = "来源类型不能为空")
    private Integer sourceType;

    /**
     * 标示id
     */
    private String flagId;


    /**
     * 成员
     * 
     */
    private List<String> members;
}
