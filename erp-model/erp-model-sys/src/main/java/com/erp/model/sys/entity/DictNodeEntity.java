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
     * 节点code 
     */
    @TableField("node_code")
    private String nodeCode;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;




    public static final String DISABLED = "disabled";

    public static final String NODE_CODE = "node_code";

    public static final String NODE_NAME = "node_name";

    public static final String SYSTEM = "system";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
