package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 通知表
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("notice")
public class NoticeEntity extends BaseEntity<NoticeEntity> {

    /**
     * 禁用状态 false 没有  true禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 通知节点code
     * 对应字典的
     */
    @TableField("node_code_dict")
    private String nodeCodeDict;


    public static final String DISABLED = "disabled";

    public static final String NODE_CODE = "node_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
