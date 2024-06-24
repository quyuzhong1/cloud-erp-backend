package com.erp.model.sys.openapi;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter
@Setter
public class CollectorPacksDTO implements Serializable {

    /**
     * 面单号
     */
    @NotBlank(message = "面单号不能为空")
    private String barCode;
    /**
     * 操作人
     */
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    /**
     * 图片路径
     */
    @NotBlank(message = "图片不能为空")
    private String imageUrl;
    /**
     * 视频路径
     */
    private String videoUrl;
    /**
     * 仓库名
     */
    private String warehouseId;
    /**
     * 包裹号
     */
    private String packageNo;
}
