package com.baomidou.mybatisplus.syslog;

import com.baomidou.mybatisplus.syslog.DetailDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 销售出库
 */
@Data
@NoArgsConstructor
public class SoOutstockDTO implements Serializable {

    /**
     * 出库单号
     */
    private String code;

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 出库单号
         */
        private String code;

        private List<DetailDTO.ViewDTO> detailList;
    }
}
