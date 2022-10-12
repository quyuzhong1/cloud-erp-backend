package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 修改产品信息
 * @Description TODO
 * @Date 2022-09-28 17:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateProductDTO  implements Serializable {

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;



    /**
     * 等级
     */
    private String grade;

    /**
     * 等级id
     */
    private String gradeId;


    /**
     * 产品负责人
     */
    private String productChargeId;


    /**
     * 产品负责人名
     */
    private String productChargeName;


    /**
     * 项目id
     */
    private String projectId;


    /**
     * 项目负责人
     */
    private String projectChargeId;


    /**
     * 项目负责人名
     */
    private String projectChargeName;

    /**
     * 项目状态
     * 项目状态 0 未启动 1 ;已启动 2 进行中 3 已完成  4 已终止
     */
    @StateEnumValue(intValues = {0,1,2,3,4}, message = "项目状态有误")
    private Integer projectStatus;


    /**
     * 立项状态
     * 0 待规划 1 调研中  2：ID设计中  3::已立项  4：已终止
     */
    @StateEnumValue(intValues = {0,1,2,3,4}, message = "立项状态有误")
    private Integer approvalStatus;

}
