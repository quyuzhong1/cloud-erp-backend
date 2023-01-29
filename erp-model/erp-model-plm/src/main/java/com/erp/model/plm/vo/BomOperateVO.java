package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname BomOperateVO
 * @Description TODO
 * @Date 2023-01-11 17:53
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomOperateVO implements Serializable {

    /**
     * 表id
     */
    private String id;


    /**
     * 创建人id
     */
    private String createUserId;


    private String createUserName;

    /**
     * 创建时间
     */
    private String createTime;


    /**
     * 操作类型 add 新建  delete 删除 update 编辑  stateUpdate 状态变更
     */
    private String type;

    private String typeName;

    /**
     * 内容
     */
    private String content;

}
