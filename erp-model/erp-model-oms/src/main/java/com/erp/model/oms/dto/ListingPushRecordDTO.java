package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ListingPushRecordDTO implements Serializable {


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        private String type;

    }


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * listingId
         */
        private String listingId;

        /**
         * sku
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 库存sku
         */
        private String platformSkuNo;
        /**
         * 库存sku 名称
         */
        private String platformSkuName;

        /**
         * 平台
         */
        private String platform;

        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 货主编码
         */
        private String ownerCode;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 最新推送时间
         */
        private LocalDateTime latestPushTime;

        private String returnMsg;
    }

}
