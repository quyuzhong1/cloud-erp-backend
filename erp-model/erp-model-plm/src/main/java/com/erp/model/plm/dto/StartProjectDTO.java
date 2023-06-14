package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;

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
    @NotBlank(message = "负责人id不能为空")
    private String  chargeId;


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
    private LocalDate startTime;


    /**
     * 结束时间
     */
    private LocalDate endTime;

    //
    /**
     * 描述
     */
    private String describe;





}
