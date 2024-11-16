package com.erp.tms.aliexpress.model.order.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@NoArgsConstructor
public class AllCarrierResponse implements Serializable {
    private Result result;
    private String request_id;


    @Data
    @NoArgsConstructor
    public static class Result {
        private CarrierData data;
        private boolean success;

    }

    @Data
    @NoArgsConstructor
    public static class CarrierData {
        private CourierList courier_list;
    }


    @Data
    @NoArgsConstructor
    public static class CourierList {
        private List<Courier> courierlist;
    }

    @Data
    @NoArgsConstructor
    public static class Courier {
        private String courier_name;
        private String courier_code;

    }
}
