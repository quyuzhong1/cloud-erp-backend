package com.erp.model.wms.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.extension.TStkCloseProfileDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 库存关账记录表
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("inventory_closed_record")
public class InventoryClosedRecordEntity extends BaseEntity<InventoryClosedRecordEntity> {

    /**
     * 金蝶关账类型:STK=库存关账,HS=存货核算关账
     */
    @TableField("category")
    private String category;

    /**
    * 关账日期
    */
    @TableField("closed_date")
    private LocalDate closedDate;
    /**
    * 库存组织id
    */
    @TableField("inventory_org_id")
    private String inventoryOrgId;
    /**
    * 库存组织名称
    */
    @TableField("inventory_org_name")
    private String inventoryOrgName;
    /**
    * 库存组织描述
    */
    @TableField("inventory_org_desc")
    private String inventoryOrgDesc;


    public static final String CLOSED_DATE = "closed_date";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    public static final String INVENTORY_ORG_DESC = "inventory_org_desc";

    /**
     * 初始化
     */
    public static InventoryClosedRecordEntity init(TStkCloseProfileDTO dto, BaseIdDTO.CodeDTO codeDTO) {
        return new InventoryClosedRecordEntity()
                .setClosedDate(dto.getFCloseDate())
                .setInventoryOrgId(codeDTO.getId())
                .setInventoryOrgName(codeDTO.getName())
                .setInventoryOrgDesc(dto.getFCategory())
                .setCategory(dto.getFCategory())
                ;
    }

    /**
     * 初始化
     */
    public static InventoryClosedRecordEntity[] initList(List<TStkCloseProfileDTO> list, BaseIdDTO.CodeDTO codeDTO) {
        return list.stream().map(obj -> init(obj, codeDTO)).toArray(InventoryClosedRecordEntity[]::new);
    }


    /**
     * 检查关账时间是否更新
     */
    public boolean isUpdateClosedDate(List<InventoryClosedRecordEntity> list) {
        if (CollectionUtils.isEmpty(list)){
            return false;
        }
        //是否存在不一致数据
        long count = list.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getCategory(), this.category)
                && CharSequenceUtil.equals(this.inventoryOrgId,obj.getInventoryOrgId())
                && !this.closedDate.isEqual(obj.getClosedDate())
        ).count();
        return count > MathUtil.ZERO ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 更新关账时间
     */
    public InventoryClosedRecordEntity setClosedDateByMap(List<InventoryClosedRecordEntity> list) {
        if (CollectionUtils.isEmpty(list)){
            return this;
        }
        //匹配数据
        InventoryClosedRecordEntity entity = list.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getCategory(), this.category)
                && CharSequenceUtil.equals(this.inventoryOrgId,obj.getInventoryOrgId())
                && !this.closedDate.isEqual(obj.getClosedDate())
        ).findFirst().orElse(null);
        //更新关账时间
        if (ObjectUtil.isNotEmpty(entity)) {
            this.setClosedDate(entity.getClosedDate());
        }
        return this;
    }
}