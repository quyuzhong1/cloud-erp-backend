package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 费用返还详情(凭据)
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_refund_voucher")
public class MouldRefundVoucherEntity extends BaseEntity<MouldRefundVoucherEntity> {

    /**
    * 模具id
    */
    @TableField("mould_detail_id")
    private String mouldDetailId;
    /**
    * 文件地址
    */
    @TableField("file_url")
    private String fileUrl;
    /**
    * 文件名字
    */
    @TableField("file_name")
    private String fileName;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String FILE_URL = "file_url";

    public static final String FILE_NAME = "file_name";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}