package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname
 * @Description TODO
 * @Date 2022-11-07 11:05
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class NoticeMessageDTO implements Serializable {

    /**
     * 表id
     */
    private String id;

    /**
     * 节点表Id
     */
    @NotBlank(message = "节点id 不能为空")
    private String nodeId;

    /**
     * 节点名
     */
    private String nodeName;

    /**
     * 项目人列表
     */
    private List<String> itemPeopleList;

    /**
     * 项目人
     */
    private String itemPeople;

    /**
     * 项目人
     */
    private String itemPeopleName;

    /**
     * 其它人 列表
     */
    private List<String> otherPeopleList;

    /**
     * 其它人
     */
    private String otherPeople="";

    /**
     * true 开始
     * flase 关闭
     */
    private boolean state;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人
     */
    private String createUserName = "";

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 更新人
     */
    private String updateUserId;

    /**
     * 更新人
     */
    private String updateUserName = "";

    /**
     * 更新时间
     */
    private Date updateTime;
}
