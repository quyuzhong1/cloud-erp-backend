package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 直接调拨单
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
*/
@Data
@Accessors(chain = true)
public class DmpTransferInfoDTO {

    /**
     * 主键id
     */
    private String id;

    /**
    * 单据编号
    */
    private String code;

    /**
    * 单据类型
    */
    private String type;

    /**
    * 调拨类型
    */
    private String transferType;

    /**
    * 调拨类型编码
    */
    private String transferTypeCode;

    /**
    * 调入组织id
    */
    private String inOrgId;

    /**
     * 调入组织编码
     */
    private String inOrgCode;

    /**
    * 调入组织名称
    */
    private String inOrgName;

    /**
    * 调出库存组织id
    */
    private String outOrgId;

    /**
     * 调出组织编码
     */
    private String outOrgCode;

    /**
    * 调出库存组织名称
    */
    private String outOrgName;

    /**
    * 单据日期
    */
    private LocalDateTime billDate;

    /**
    * 单据状态
    */
    private String approveStatus;

    /**
    * 调拨方向
    */
    private String transferDirection;

    /**
    * 平台创建人
    */
    private String platformCreateUserName;

    /**
    * 平台创建时间
    */
    private LocalDateTime platformCreateTime;

    /**
    * 审核人
    */
    private String approveUserName;


    /**
     * 仓管员
     */
    private String WarehouseKeeperCode;

    /**
    * 审核时间
    */
    private LocalDateTime approveTime;

    /**
    * 作废状态（false未作废，true已作废）
    */
    private Boolean invalidStatus;

    /**
    * 作废时间
    */
    private LocalDateTime invalidTime;

    /**
    * 作废人
    */
    private String invalidUserName;

    /**
    * 最后修改时间
    */
    private LocalDateTime lastUpdatedTime;

    /**
    * 最后修改人
    */
    private String lastUpdatedUserName;

    /**
    * 第三方单据id
    */
    private String sourceId;

    /**
    * 来源平台 马帮，管易，金蝶等
    */
    private String platformSign;

    /**
    * 同步马帮状态
    */
    private String syncMbStatus;

    /**
    * 最新同步时间
    */
    private LocalDateTime lastSyncMbTime;

    private String remark;


    /**
     * 调拨单明细
     */
    @TableField(exist = false)
    private List<DmpTransferInfoDetailDTO> detailList;

}