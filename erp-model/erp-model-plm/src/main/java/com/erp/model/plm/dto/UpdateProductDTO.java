package com.erp.model.plm.dto;

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

    @NotBlank(message = "产品id不能为空")
    private String productId;


    //等级
    private String grade;

    //产品负责人
    private String productChargeId;

    //产品负责人名
    private String productChargeName;


    private String projectId;

    //项目负责人
    private String projectChargeId;

    //项目负责人名
    private String projectChargeName;

    //项目状态
    private Integer projectStatus;

    //立项状态状态
    private Integer approvalStatus;

}
