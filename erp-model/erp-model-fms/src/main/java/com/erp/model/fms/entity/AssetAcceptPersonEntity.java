package com.erp.model.fms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.fms.enums.PersonTypeEnum;


/**
 * <p>
 * 资产验收人员关联表
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_accept_person")
public class AssetAcceptPersonEntity extends BaseEntity<AssetAcceptPersonEntity> {

    /**
    * 资产验收ID
    */
    @TableField("asset_accept_id")
    private String assetAcceptId;
    /**
    * 人员类型：purchaseDev-采购开发人员,qualityEngineer-质量工程师,structureEngineer-结构工程师,productManager-产品经理,projectManager-项目经理
    */
    @TableField("person_type")
    private String personType;
    /**
    * 人员ID
    */
    @TableField("user_id")
    private String userId;
    /**
    * 人员姓名
    */
    @TableField("user_name")
    private String userName;


    public static final String ASSET_ACCEPT_ID = "asset_accept_id";

    public static final String PERSON_TYPE = "person_type";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    /**
     * 获取人员类型枚举
     * @return 枚举
     */
    public PersonTypeEnum getPersonTypeEnum() {
        return PersonTypeEnum.getByCode(this.personType);
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}