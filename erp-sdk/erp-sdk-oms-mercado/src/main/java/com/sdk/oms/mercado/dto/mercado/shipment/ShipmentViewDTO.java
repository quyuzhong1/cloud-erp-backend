package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class ShipmentViewDTO {

    /**
     * id : 43116658829
     * order_id : 0
     * status : delivered
     * substatus :
     * declared_value : 21.2
     * currency_id : USD
     * date_created : 2024-02-18T22:28:18.428-04:00
     * last_updated : 2024-03-01T12:33:59.547-04:00
     * tracking_number : MMXQP146592744R
     * tracking_method : MailAmericasNoRush
     * origin : {"sender_id":1511265855,"shipping_address":{"address_id":1331228739,"address_line":"XXXXXXX","street_name":"XXXXXXX","street_number":"XXXXXXX","comment":null,"zip_code":"XXXXXXX","city":{"id":"SEstSEtLd2FpIFRzaW5n","name":"Kwai Tsing"},"state":{"id":"HK-HK","name":"Hong Kong"},"country":{"id":"HK","name":"Hong Kong"},"neighborhood":{"id":null,"name":null},"municipality":{"id":null,"name":null},"agency":{"agency_id":null,"carrier_id":null,"description":null,"open_hours":null,"phone":null,"type":null},"types":["billing","default_selling_address","shipping"],"latitude":0,"longitude":0,"geolocation_type":null,"geolocation_last_updated":null,"geolocation_source":null,"delivery_preference":""},"type":"selling_address"}
     * destination : {"type":"buying_address","receiver_id":139133205,"receiver_name":"Wilbert Geovany Alonzo Graniel","receiver_phone":"9847459241","comments":"","shipping_address":{"address_id":271976606,"address_line":"2 Poniente 196","street_name":"2 Poniente","street_number":"196","comment":"M23L7 Referencia: M23L7","zip_code":"77760","city":{"id":"TVgtUk9PVHVsdW0","name":"Tulum"},"state":{"id":"MX-ROO","name":"Quintana Roo"},"country":{"id":"MX","name":"Mexico"},"neighborhood":{"id":null,"name":"Guerra de Castas"},"municipality":{"id":null,"name":"Tulum"},"agency":{"agency_id":null,"carrier_id":null,"description":null,"open_hours":null,"phone":null,"type":null},"types":["billing","default_buying_address","default_return_address","shipping"],"latitude":20.21588,"longitude":-87.455037,"geolocation_type":"ROOFTOP","geolocation_last_updated":"2023-10-09T22:22:49.854Z","geolocation_source":"map-verified","delivery_preference":"business"}}
     * dimensions : {"height":6,"length":11,"weight":190,"width":5}
     * external_reference : null
     * lead_time : {"option_id":1338705174,"shipping_method":{"id":509450,"type":"standard","name":"Estándar a domicilio","deliver_to":"address"},"currency_id":"USD","cost":0,"list_cost":5.46,"cost_type":"free","service_id":628333,"estimated_delivery_time":{"type":"known_frame","date":"2024-03-01T00:00:00.000-06:00","unit":"hour","offset":{"date":"2024-03-14T00:00:00.000-06:00","shipping":216},"time_frame":{"from":"","to":""},"pay_before":"2024-02-19T10:00:00.000-06:00","shipping":168,"handling":72,"schedule":null}}
     * source : {"site_id":"MLM"}
     * logistic : {"mode":"me2","type":"drop_off","direction":"forward"}
     * tags : []
     * sender_id : 0
     */

    @SerializedName("id")
    private long id;
    @SerializedName("order_id")
    private int orderId;
    @SerializedName("status")
    private String status;
    @SerializedName("substatus")
    private String substatus;
    @SerializedName("declared_value")
    private double declaredValue;
    @SerializedName("currency_id")
    private String currencyId;
    @SerializedName("date_created")
    private String dateCreated;
    @SerializedName("last_updated")
    private String lastUpdated;
    @SerializedName("tracking_number")
    private String trackingNumber;
    @SerializedName("tracking_method")
    private String trackingMethod;
    @SerializedName("origin")
    private OriginBean origin;
    @SerializedName("destination")
    private DestinationBean destination;
    @SerializedName("dimensions")
    private DimensionsBean dimensions;
    @SerializedName("external_reference")
    private Object externalReference;
    @SerializedName("lead_time")
    private LeadTimeBean leadTime;
    @SerializedName("source")
    private SourceBean source;
    @SerializedName("logistic")
    private LogisticBean logistic;
    @SerializedName("sender_id")
    private int senderId;
    @SerializedName("tags")
    private List<?> tags;

}
