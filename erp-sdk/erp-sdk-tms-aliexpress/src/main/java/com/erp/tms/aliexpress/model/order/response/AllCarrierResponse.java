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
    public static class Result implements Serializable  {
        private CarrierData data;
        private boolean success;

    }

    @Data
    @NoArgsConstructor
    public static class CarrierData implements Serializable {
        private CourierList courierList;
    }


    @Data
    @NoArgsConstructor
    public static class CourierList implements Serializable {
        private List<Courier> courier_list;
    }

    @Data
    @NoArgsConstructor
    public static class Courier implements Serializable {
        private String courier_name;
        private String courier_code;

    }
}
