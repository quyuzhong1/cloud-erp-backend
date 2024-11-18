package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * bom 操作记录日志表(BomOperateLog)实体类
 *
 * @author yl
 * @since 2023-01-09 11:32:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_operate_log")
public class BomOperateLogEntity extends BaseEntity<BomOperateLogEntity> implements Serializable {
    private static final long serialVersionUID = -32922189918551527L;

    /**
     * bom 表id
     */
    private String bomId;
    /**
     * 操作类型 add 新建  delete 删除 update 编辑  stateUpdate 状态变更 
     */
    private String type;
    /**
     * 变更内容
     */
    private String content;



}

