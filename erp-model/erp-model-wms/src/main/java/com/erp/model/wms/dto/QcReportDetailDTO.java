package com.erp.model.wms.dto;

import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 质检单
 * 质检报告明细
 *
 * @author Lambda
 * @Classname QcReportDetailDTO

 * @Date 2023-04-17 10:10
 * @Created by yl
 */
@Data
public class QcReportDetailDTO {
    private QcReportDetailDTO() {
        throw new IllegalStateException("Utility QcReportDetailDTO class");
    }

    /**
     * 添加的质检报告
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {
        private static final long serialVersionUID = 1905122041950251207L;

        private String id;

        /**
         * 质检项
         */
        @NotBlank(message = "质检报告不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private String qcReportId;

        /**
         * 质检说明
         */
        @Size(max = 250, message = "质检说明不能超过250个字符", groups = {UpdateGroup.class, AddGroup.class})
        private String description;

        /**
         * 质检结果
         * 来源 http://172.16.100.11:3002/project/92/interface/api/7186
         */
        private String resultDict;

        /**
         * 质检报告附件url
         */
        private List<String> reportUrlList;

        /**
         * 质检报告名称
         */
        private List<String> reportNameList;

    }


    /**
     * 添加的质检报告
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;


        private String id;

        /**
         * 质检报告id
         */
        private String qcReportId;


        /**
         * 质检项
         */
        private String qcReportName;


        /**
         * 质检内容
         */
        private String qcReportContent;

        /**
         * 质检说明
         */
        private String description;

        /**
         * 质检结果
         * 来源 http://172.16.100.11:3002/project/92/interface/api/7186
         */
        private String resultDict;

        /**
         * 质检结果 名
         */
        private String resultName;


        /**
         * 质检报告附件url
         */
        private List<String> reportUrlList;

        /**
         * 质检报告名称
         */
        private List<String> reportNameList;

    }


    /**
     * 导出质检报告
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {


        @NotBlank(message = "质检类型不能为空")
        private String qcType;
    }


    /**
     * 导出质检报告
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 成功返回数据
         */
        private List<QcReportDetailDTO.ListDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 质检报告id
         */
        private String qcReportId;


        /**
         * 质检项
         */
        private String qcReportName;


        /**
         * 质检内容
         */
        private String qcReportContent;

        /**
         * 质检说明
         */
        private String description;

        /**
         * 质检结果
         * 来源 http://172.16.100.11:3002/project/92/interface/api/7186
         */
        private String resultDict;
    }
}
