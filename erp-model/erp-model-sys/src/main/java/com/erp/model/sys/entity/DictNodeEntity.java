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
 * 通知信息表
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_node")
public class DictNodeEntity extends BaseEntity<DictNodeEntity> {


    /**
     * 节点key
     */
    @TableField("node_key")
    private String nodeCode;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 业务模块
     */
    @TableField("module")
    private String module;





    @Override
    public Serializable pkVal() {
        return null;
    }

}
