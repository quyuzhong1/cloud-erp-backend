package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.dmp.entity.DmpPushWdtDetailEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 推送旺店通中间表DTO
 * @date 2024-07-24
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class DmpPushWdtDTO implements Serializable {

    @Data
    public static class CommonDTO{
        /**
         * erp单据id
         */
        private String sourceId;

        /**
         * erp单据编码
         */
        private String sourceCode;

        /**
         * 推送给旺店通的单据编码（outer_no）
         */
        private String thirdCode;

        /**
         * erp仓库id
         */
        private String warehouseId;

        /**
         * 第三方仓库编码
         */
        private String thirdWarehouseCode;

        /**
         * 推送到旺店通的单据类型：其他入库单/其他出库单
         */
        private String thirdType;

        /**
         * 操作类型：审核/反审核
         */
        private String operateType;

        /**
         * 明细
         */
        private List<DmpPushWdtDetailDTO> detailDTOList;

        /**
         * 映射状态
         */
        private String mappingStatus;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AddDTO extends CommonDTO{

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ViewDTO extends CommonDTO{
        private String id;
    }
}
