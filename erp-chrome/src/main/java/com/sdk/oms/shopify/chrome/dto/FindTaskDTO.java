package com.sdk.oms.shopify.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @Classname FindTaskDTO

 * @Date 2022-08-29 14:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindTaskDTO  implements Serializable {


    @NotBlank(message = "平台不能为空")
    private String platform;
}
