package com.erp.model.wms.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcRuleDTO
 * @Description TODO
 * @Date 2023-04-13 10:00
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcRuleDTO implements Serializable {


    /**
     * 添加质检规则
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends PermissionsDTO {


        /**
         * 质检类型
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890
         * stockIn 入库质检  outsideQc 外检质检 insideQc 在库质检 newProductStockIn 新品入库质检 b2bOutsideQc B2B外检
         */
        @NotNull(message = "质检类型不能为空")
        @StateEnumValue(clazz = QcTypeEnum.class, message = "质检类型有误")
        private QcTypeEnum qcType;

        /**
         * 是否有报告
         */
        @NotNull(message = "是否含有质检报告不能为空")
        private Boolean existReport;


        /**
         * 产品等级 plm 系统
         * 来源 http://172.16.100.11:3002/project/47/interface/api/4505  请传里面对应的 value
         */
        private List<String> productGradeKeyList;


        /**
         * 质检报告集合
         */
        private List<QcReportDTO.AddDTO> qcReportList;

    }


    /**
     * 修改质检规则
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends PermissionsDTO {


        private String id;


        /**
         * 质检类型
         */
        @NotNull(message = "质检类型不能为空")
        @StateEnumValue(clazz = QcTypeEnum.class, message = "质检类型有误")
        private QcTypeEnum qcType;

        /**
         * 是否有报告
         */
        @NotNull(message = "是否含有质检报告不能为空")
        private Boolean existReport;


        /**
         * 产品等级
         */
        private List<String> productGradeKeyList;


        /**
         * 质检报告集合
         */
        private List<QcReportDTO.UpdateDTO> qcReportList;

    }


    /**
     * 质检规则详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {


        /**
         * code
         */
        private String code;


        /**
         * 是否禁用
         * true 禁用
         * false 启用
         */
        private Boolean disabled;


        /**
         * 质检类型
         */
        private String qcTypeName;

        /**
         * 审核状态名
         */
        private String approveStatusName;

        /**
         * 审核状态
         */
        private String approveStatus;


    }

    /**
     * 质检规则的分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;

        /**
         * code
         */
        private String code;


        /**
         * 质检类型
         */
        private String qcTypeName;

        private QcTypeEnum qcType;

        /**
         * 是否有质检报告
         */
        private Boolean existReport;


        /**
         * 是否禁用
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 审核状态名
         */
        private String approveStatusName;


        /**
         * 审核状态
         */
        private String approveStatus;


        /**
         * 创建人名称
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;


    }


    /**
     * 质检规则的分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        private String searchKeyword;

    }

}
