package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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


    @NotBlank(message = "负责人不能为空")
    private String  chargeName;

    @NotBlank(message = "负责人id不能为空")
    private String  chargeId;

    @NotBlank(message = "产品id不能为空")
    private String  productId;



    private Date startTime;


    private Date endTime;

    //描述
    private String describe;


    @StateEnumValue(intValues = {0,1,2}, message = "来源类型不能为空")
    private Integer sourceType;

    private String flagId;


    private List<ProjectMemberDTO> members;
}
