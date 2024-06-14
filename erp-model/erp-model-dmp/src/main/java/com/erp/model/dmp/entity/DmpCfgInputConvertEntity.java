package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 外部系统接口转换内部数据
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_cfg_input_convert")
public class DmpCfgInputConvertEntity extends BaseEntity<DmpCfgInputConvertEntity> {

    /**
    * 输入信息id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 输入任务存储状态：fds=文件系统，mongo=mongo,dmp=中台  枚举：DmpCfgInputConvertInputStatusEnum
    */
    @TableField("input_status")
    private String inputStatus;
    /**
    * 拉取数据类型
    */
    @TableField("type")
    private String type;
    /**
    * 数据的存储名，fds为文件夹路径，mongo为集合名,pg为表名
    */
    @TableField("storage_name")
    private String storageName;
    /**
    * 转换逻辑处理类
    */
    @TableField("convert_class")
    private String convertClass;
    /**
    * 唯一属性字段名,为空不检验重复，{all}为所有字段，多个以逗号隔开
    */
    @TableField("unique_field_name")
    private String uniqueFieldName;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    
    /**
     * 优先级
     */
     @TableField("order")
     private Integer order;


    public static final String MAIN_ID = "main_id";

    public static final String INPUT_STATUS = "input_status";

    public static final String TYPE = "type";

    public static final String STORAGE_NAME = "storage_name";

    public static final String CONVERT_CLASS = "convert_class";

    public static final String UNIQUE_FIELD_NAME = "unique_field_name";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
