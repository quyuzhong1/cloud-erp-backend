package com.sdk.wms.goodcang.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodCangUploadOrderLabelReq {

    // 订单号 必填
    @JSONField(name = "order_code")
    @NotBlank(message = "订单号不能为空")
    private String orderCode;

    //物流跟踪号 必填
    @JSONField(name = "tracking_number")
    @NotBlank(message = "物流跟踪号不能为空")
    private Integer trackingNumber;

    //分拣码
    @JSONField(name = "package_area_code")
    private String packageAreaCode;

    //面单内容
    @JSONField(name = "label_info")
    private LabelInfo labelInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LabelInfo {

        //面单格式 PDF
        @JSONField(name = "label_image_type")
        @NotBlank(message = "面单格式")
        private Integer labelImageType;

        //面单URL数组
        @JSONField(name = "label_url_list")
        private List<String> labelUrlList;

        //附件ID列表
        @JSONField(name = "label_id_list")
        private List<Integer> labelIdList;
    }
}
