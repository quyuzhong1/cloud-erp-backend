package com.baomidou.mybatisplus.syslog;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 销售出库
 *
 */
@Data
@NoArgsConstructor
public class DetailDTO implements Serializable {
    /**
     * 出库详情单号
     */
    private String detailCode;


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * 出库详情单号
         */
        private String detailCode;

    }
}
