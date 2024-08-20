package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 第三方仓库
 * </p>
 *
 * @author shukai
 * @since 2024-08-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_warehouse_info")
public class DmpThirdWarehouseInfoEntity extends BaseEntity<DmpThirdWarehouseInfoEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 仓库平台类型
    */
    @TableField("warehouse_platform_type")
    private String warehousePlatformType;
    /**
    * 来源平台（编码）：goodcang、iml
    */
    @TableField("source_platform")
    private String sourcePlatform;
    /**
    * erp授权Id
    */
    @TableField("auth_id")
    private String authId;
    /**
    * 仓库编码
    */
    @TableField("warehouse_code")
    private String warehouseCode;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 国家编码
    */
    @TableField("country_code")
    private String countryCode;
    
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
     * 邮编
     */
    @TableField("zip")
    private String zip;
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
     * 子类型：0:默认,1:旺店通,2:菜鸟,3:百世WMS,4:巨沃,5:心怡WMS,6:科捷,7:吉客云,8:中通WMS,9:通天晓,10:酷仓宝WMS,11:景天WMS,12:网店管家笛佛WMS,13:九曳WMS,14:万里牛,15:麓客WMS,16:青图WMS(ERP),17:安鲜达WMS,18:顺丰WMS,19苏宁WMS,20:雅澳E,21:EMS,22:递四方,23:中邮WMS,24:云腾WMS,25:天图WMS;26:但丁WMS,27:e仓宝,28:仓卫士,29:山橙WMS,30橙蚁WMS,31:中山邮政WMS,32:赢路WMS,33:无忧WMS,34:筋斗云WMS,35:韵达WMS,36:GEEK
     */
    @TableField("sub_type")
    private String subType;
     
     /**
      * 备注
      */
    @TableField("remark")
    private String remark;
    
    /**
     * 分类，实体仓和虚拟仓
     */
   @TableField("category")
   private String category;
   
   /**
    * 虚拟仓对应的实体仓
    */
  @TableField("warehouse_list")
  private String warehouseList;
    
    /**
     * 是否禁用/停用 true 是 false 不是
     */
    private Boolean disabled;
    
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String WAREHOUSE_PLATFORM_TYPE = "warehouse_platform_type";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String AUTH_ID = "auth_id";

    public static final String WAREHOUSE_CODE = "warehouse_code";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    @Override
    public Serializable pkVal() {
        return null;
    }

}