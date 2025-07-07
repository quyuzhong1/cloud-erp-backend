package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 发货单箱子信息明细表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_carton")
public class WmsCartonEntity extends BaseEntity<WmsCartonEntity> {

    /**
    * 箱规id
    */
    @TableField("spec_id")
    private String specId;
    /**
    * 箱号
    */
    @TableField("box_no")
    private Integer boxNo;
    /**
    * 异常原因
    */
    @TableField("error_msg")
    private String errorMsg;
    /**
     * 任务表id
     */
    @TableField("packing_task_id")
    private String packingTaskId;
    /**
     * 单箱状态(incomplete 未完成,completed 已完成)
     * PackingTaskStatusEnum
     * 字典接口地址  http://172.16.100.11:3002/project/92/interface/api/13147  type = packingStatusSingle 单箱 / packingStatus总
     */
    @TableField("packing_status")
    private String packingStatus;
    /**
     * 称重状态-单箱(unweighed 未称重,success 称重成功,fail 称重失败 )
     * PackingWeightStatusEnum
     * 字典接口地址  http://172.16.100.11:3002/project/92/interface/api/13147 type= weightingStatusSingle单箱 /  weightingStatus 总
     */
    @TableField("weighting_status")
    private String weightingStatus;
    /**
     * 装箱员id
     */
    @TableField("packing_user_id")
    private String packingUserId;
    /**
     * 装箱员名称
     */
    @TableField("packing_user_name")
    private String packingUserName;
    /**
     * 是否释放库存
     */
    @TableField("is_release_inventory")
    private Boolean isReleaseInventory;


    public static final String CARTON_ID = "carton_id";

    public static final String CARTON_DETAIL_ID = "carton_detail_id";

    public static final String BOX_NO = "box_no";

    public static final String BOX_DESC = "box_desc";

    @Override
    public Serializable pkVal() {
        return null;
    }

}