package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 第三方系统仓库表
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_warehouse")
public class ThirdWarehouseEntity extends BaseEntity<ThirdWarehouseEntity> {

    /**
    * 是否禁用/停用 true 是 false 不是
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 系统类型：lingxing领星，wangdian旺店通
    */
    @TableField("sys_type")
    private String sysType;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 类型:1:普通（内部）,2:自流转,3:平台,4:京东沧海.6:抖音云仓,125:代发仓,126:分销委外
    */
    @TableField("type")
    private String type;
    /**
    * 子类型：0:默认,1:旺店通,2:菜鸟,3:百世WMS,4:巨沃,5:心怡WMS,6:科捷,7:吉客云,8:中通WMS,9:通天晓,10:酷仓宝WMS,11:景天WMS,12:网店管家笛佛WMS,13:九曳WMS,14:万里牛,15:麓客WMS,16:青图WMS(ERP),17:安鲜达WMS,18:顺丰WMS,19苏宁WMS,20:雅澳E,21:EMS,22:递四方,23:中邮WMS,24:云腾WMS,25:天图WMS;26:但丁WMS,27:e仓宝,28:仓卫士,29:山橙WMS,30橙蚁WMS,31:中山邮政WMS,32:赢路WMS,33:无忧WMS,34:筋斗云WMS,35:韵达WMS,36:GEEK
    */
    @TableField("sub_type")
    private String subType;
    /**
    * 编号
    */
    @TableField("code")
    private String code;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 地址 
    */
    @TableField("address")
    private String address;
    /**
    * 联系人
    */
    @TableField("contacts")
    private String contacts;
    /**
    * 联系人电话
    */
    @TableField("tel_number")
    private String telNumber;
    /**
    * 固话
    */
    @TableField("telno")
    private String telno;
    /**
    * 邮箱
    */
    @TableField("email")
    private String email;
    /**
    * 邮编
    */
    @TableField("zip")
    private String zip;
    /**
    * 网址
    */
    @TableField("website")
    private String website;
    /**
    * 省份
    */
    @TableField("province")
    private String province;
    /**
    * 城市
    */
    @TableField("city")
    private String city;
    /**
    * 区县
    */
    @TableField("district")
    private String district;
    /**
    * 第三方创建时间
    */
    @TableField("created")
    private String created;
    /**
    * 第三方修改时间
    */
    @TableField("modified")
    private String modified;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 分类
     */
    @TableField("category")
    private String category;
    /**
     * 当分类是虚拟仓时，会存储第三方虚拟仓关联实体仓信息
     */
    @TableField("warehouse_list")
    private String warehouseList;

    /**
     * 库存同步模式
     * InventorySyncModeEnum
     * inventory 库存同步 ，order 单据同步
     */
    @TableField("inventory_sync_mode")
    private String inventorySyncMode;

    public static final String DISABLED = "disabled";

    public static final String SYS_TYPE = "sys_type";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String TYPE = "type";

    public static final String SUB_TYPE = "sub_type";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String ADDRESS = "address";

    public static final String CONTACTS = "contacts";

    public static final String TEL_NUMBER = "tel_number";

    public static final String TELNO = "telno";

    public static final String EMAIL = "email";

    public static final String ZIP = "zip";

    public static final String WEBSITE = "website";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String DISTRICT = "district";

    public static final String CREATED = "created";

    public static final String MODIFIED = "modified";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}