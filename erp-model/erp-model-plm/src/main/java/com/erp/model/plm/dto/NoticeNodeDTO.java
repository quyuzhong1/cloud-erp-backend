package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname NoticeNodeDTO
 * @Description TODO
 * @Date 2022-11-07 10:39
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class NoticeNodeDTO implements Serializable {


    private String id;


    @NotBlank(message = "节点名不能为空")
    private String nodeName;

    @NotBlank(message = "节点标示不能为空")
    private String nodeFlag;



}
