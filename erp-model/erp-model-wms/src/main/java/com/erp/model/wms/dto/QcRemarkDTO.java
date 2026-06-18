package com.erp.model.wms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcRemarkDTO

 * @Date 2023-04-18 17:07
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcRemarkDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;

        /**
         * id
         */
        private String id;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人
         */
        private String createUserName;


        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO  extends AddDTO{


        private String mainId;

    }

    @Data
    @NoArgsConstructor
    public static class QcResultView {
        /**
         * 质检类型
         */
        @NotBlank(message = "质检类型不允许为空")
        private String qcType;
        /**
         * 质检类型
         */
        private String qcTypeName;
        /**
         * 质检数量
         */
        @NotNull(message = "质检数量不允许为空")
        private Integer qcQty;

        /**
         * 总数量
         */
        @NotNull(message = "总数量不允许为空")
        private Integer totalQty;

        /**
         * 检验结果 QcResultEnum
         */
        @NotBlank(message = "检验结果不允许为空")
        private String qcResult;
        /**
         * 检验结果名称
         */
        private String qcResultName;
        /**
         * 是否内检
         */
        @NotNull(message = "是否内检不允许为空")
        private Boolean isInside;
        /**
         * 是否库内抽检
         */
        @NotNull(message = "是否库内抽检不允许为空")
        private boolean isInsideQc;
        /**
         * 检验合格量
         */
        @NotNull(message = "检验合格量不允许为空")
        private Integer qcGoodQty;
        /**
         * 检验合格率
         */
        private BigDecimal qcGoodRate;
        /**
         * 批次合格量
         */
        @NotNull(message = "批次合格量不允许为空")
        private Integer lotQualifiedQty;
        /**
         * 抽检结果
         */
        private String qcSampleResult;
        /**
         * 抽样比例
         */
        private BigDecimal qcSamplingRate;
        /**
         * 检验不良量
         */
        @NotNull(message = "检验不良量不允许为空")
        private Integer qcBadQty;
        /**
         * 允许入库量
         */
        @NotNull(message = "允许入库量不允许为空")
        private Integer allowInstockQty;
        /**
         * 检验不良率
         */
        private String qcBadRate;
        /**
         * 处理措施type=handleModeType
         */
        @NotBlank(message = "处理措施不允许为空")
        private String handleModeDict;
        /**
         * 附件url
         */
        private String attachurl;
        /**
         * 附件名称
         */
        private String attachName;

        /**
         * 产品实物图片地址集合（从产品信息移动到质检信息）
         */
        private List<String> productRealImageUrlList;

        /**
         * 产品实物图片名称集合
         */
        private List<String> productRealImageNameList;

        /**
         * 箱唛图片地址集合（从产品信息移动到质检信息）
         */
        private List<String> boxImageUrlList;

        /**
         * 箱唛图片名称集合
         */
        private List<String> boxImageNameList;

    }
}
