package com.erp.model.dmp.dto;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 亚马逊 任务参数 DTO
 *
 * @Author Jim
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AmazonJobParamDTO {

    /**
     * 检查最新【亚马逊报告】亚马逊-ERP-DTO
     */
    @Data
    @AllArgsConstructor
    public static class ReportBaseDTO {
        /**
         * 每条线程执行记录数
         */
        private Integer size;

        /**
         * 指定店铺IDS
         */
        private List<String> shopIdList;

        /**
         * 指定报告类型列表
         */
        private List<String> recordTypeList;

        public ReportBaseDTO() {
            this.size = 10;
            this.shopIdList = Collections.emptyList();
            this.recordTypeList = Collections.emptyList();
        }

        public static ReportBaseDTO init(String jobParamStr) {
            ReportBaseDTO initDTO = new ReportBaseDTO();
            if (StringUtils.isBlank(jobParamStr)) {
                return initDTO;
            }
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            fillJobParams(jobParam, initDTO);
            return initDTO;
        }

        public static void fillJobParams(JSONObject jobParam, ReportBaseDTO reportBaseDTO) {
            Integer sizeInt = jobParam.getInteger("size");
            if (null != sizeInt) {
                reportBaseDTO.setSize(sizeInt);
            }
            String shopIdListStr = jobParam.getString("shopIdList");
            if (StringUtils.isNotBlank(shopIdListStr)) {
                List<String> shopIdList = JSONUtil.toList(shopIdListStr, String.class);
                reportBaseDTO.setShopIdList(shopIdList);
            }
            String recordTypeListStr = jobParam.getString("recordTypeList");
            if (StringUtils.isNotBlank(recordTypeListStr)) {
                List<String> recordTypeList = JSONUtil.toList(recordTypeListStr, String.class);
                reportBaseDTO.setRecordTypeList(recordTypeList);
            }
        }
    }


    /**
     * 创建【亚马逊报告】亚马逊-ERP-DTO
     */
    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ReportJobDTO extends ReportBaseDTO {


        /**
         * 是否忽略下次执行时间
         */
        private Boolean ignoreNextReportCreationTime;


        public ReportJobDTO(int defaultSize) {
            super.size = defaultSize;
            super.shopIdList = Collections.emptyList();
            super.recordTypeList = Collections.emptyList();
            this.ignoreNextReportCreationTime = false;
        }

        public static ReportJobDTO init(String jobParamStr, int defaultSize) {
            ReportJobDTO reportJobDTO = new ReportJobDTO(defaultSize);
            if (StringUtils.isBlank(jobParamStr)) {
                return reportJobDTO;
            }
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            ReportBaseDTO.fillJobParams(jobParam, reportJobDTO);
            Boolean ignoreNextReportCreationTime = jobParam.getBoolean("ignoreNextReportCreationTime");
            if (null != ignoreNextReportCreationTime) {
                reportJobDTO.setIgnoreNextReportCreationTime(ignoreNextReportCreationTime);
            }
            return reportJobDTO;
        }
    }


}
