package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private LocalDateTime startTime;


    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private LocalDateTime endTime;

    //
    /**
     * 描述
     */
    private String describe;


}
