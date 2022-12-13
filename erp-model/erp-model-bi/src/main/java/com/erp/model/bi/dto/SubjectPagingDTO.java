package com.erp.model.bi.dto;

import com.erp.common.modules.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SubjectPagingDTO
 * @Description TODO
 * @Date 2022-12-13 12:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SubjectPagingDTO implements Serializable {

    /**
     * 表id
     */
    @NotBlank(message = "id不能为空" ,groups = UpdateGroup.class )
    private String id;


    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分类名
     */
    private String categoryName;

    /**
     * 专题名
     */
    private String name;

    /**
     * 是否常用
     * 0 不是 1 是
     */
    private Integer isFrequently;

    /**
     * 是否启用
     * true 启用
     * false 没有
     */
    private Boolean state;



    /**
     * 分享标示
     * personal 私人
     * share 共享
     */
    private String shareFlag;

    /**
     * 创建人id
     */
    private String createUserId;


    /**
     * 创建人名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 更改人id
     */
    private String updateUserId;

    /**
     * 更改人
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private Date updateTime;



}
