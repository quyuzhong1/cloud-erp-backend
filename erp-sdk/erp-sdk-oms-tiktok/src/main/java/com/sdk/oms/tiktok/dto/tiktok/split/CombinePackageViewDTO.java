package com.sdk.oms.tiktok.dto.tiktok.split;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class CombinePackageViewDTO {

    @SerializedName("code")
    private int code;
    @SerializedName("data")
    private CombinePackageBean data;
    @SerializedName("message")
    private String message;
    @SerializedName("request_id")
    private String requestId;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CombinePackageBean {

        @SerializedName("code")
        private String code;

        @SerializedName("message")
        private String message;

        @SerializedName("packages")
        private List<Packages> packages;

        @SerializedName("errors")
        private List<ErrorBean> errors;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Packages {

        @SerializedName("id")
        private String id;

        @SerializedName("order_ids")
        private List<String> orderIds;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorBean {

        @SerializedName("code")
        private String code;

        @SerializedName("message")
        private String message;

        @SerializedName("detail")
        private Detail detail;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {

        @SerializedName("package_id")
        private String packageId;

    }
}
