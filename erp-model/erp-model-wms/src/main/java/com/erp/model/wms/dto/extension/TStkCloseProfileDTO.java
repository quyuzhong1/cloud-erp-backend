package com.erp.model.wms.dto.extension;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 金蝶库存关账记录
 *
 * @author Jim
 * @since 2023-10-11
 */
@Data
@NoArgsConstructor
public class TStkCloseProfileDTO {

    /**
     * 金蝶表主键ID
     */
    private Integer fCloseId;

    /**
     * 金蝶关账类型:STK=库存关账
     */
    private String fCategory;

    /**
     * 金蝶关账日期
     */
    private LocalDate fCloseDate;

    /**
     * 金蝶操作人ID
     */
    private Integer fCloseRid;

    /**
     * 金蝶库存组织ID
     */
    private Integer fOrgId;

    /**
     * 金蝶操作时间
     */
    private LocalDateTime fOperateTime;
}
