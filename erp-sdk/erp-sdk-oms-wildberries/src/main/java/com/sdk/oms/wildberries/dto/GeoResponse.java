package com.sdk.oms.wildberries.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zdy
 * @ClassName GeoResponse
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@Data
public class GeoResponse {

    private String documentation;
    private List<Licenses> licenses;
    private Rate rate;
    private List<Response> results;
    private Status status;
    @JsonProperty("stay_informed")
    private StayInformed stayInformed;
    private String thanks;
    private Timestamp timestamp;
    @JsonProperty("total_results")
    private Integer totalResults;

    @Data
    public static class Licenses{
        private String name;
        private String url;
    }
    @Data
    public static class Rate{
        private Integer limit;
        private Integer remaining;
        private Long reset;
    }

    @Data
    public static class Response{
        private Annotations annotations;
        private Bound bounds;
        private Components components;
        private Integer confidence;
        @JsonProperty("distance_from_q")
        private Distance distanceFromQ;
        private String formatted;
        private Geometry geometry;
    }

    @Data
    public static class Annotations{
        private Geometry DMS;
        private String MGRS;
        private String Maidenhead;
        private Mercator Mercator;
        private Mercator OSM;
        private String geohash;
        private BigDecimal qibla;
        private Roadinfo roadinfo;
        private Sun sun;
        private Timezone timezone;
        private What3words what3words;
        private String wikidata;
    }
    @Data
    public static class Geometry{
        private String lat;
        private String lng;
    }
    @Data
    public static class Mercator{
        private String x;
        private String y;
    }
    @Data
    public static class Roadinfo{
        @JsonProperty("drive_on")
        private String driveOn;
        @JsonProperty("speed_in")
        private String speedIn;
    }
    @Data
    public static class Sun{
        private Rise rise;
        private Rise set;
    }
    @Data
    public static class Rise{
        private Long apparent;
        private Long astronomical;
        private Long civil;
        private Long nautical;
    }
    @Data
    public static class Timezone{
        private String name;
        @JsonProperty("now_in_dst")
        private Integer nowInDst;
        @JsonProperty("offset_sec")
        private Integer offsetSec;
        @JsonProperty("offset_string")
        private String offsetString;
        @JsonProperty("short_name")
        private String shortName;
    }
    @Data
    public static class What3words{
        private String words;
    }
    @Data
    public static class Bound{
        private Geometry northeast;
        private Geometry southwest;
    }
    @Data
    public static class Components{
        @JsonProperty("ISO_3166-1_alpha-2")
        private String ISO2;
        @JsonProperty("ISO_3166-1_alpha-3")
        private String ISO3;
        @JsonProperty("ISO_3166-2")
        private List<String> ISO;
        @JsonProperty("_category")
        private String category;
        @JsonProperty("_normalized_city")
        private String normalizedCity;
        @JsonProperty("_type")
        private String type;
        private String borough;
        private String city;
        private String continent;
        private String country;
        @JsonProperty("country_code")
        private String countryCode;
        private String house_number;
        private String neighbourhood;
        private String office;
        @JsonProperty("political_union")
        private String politicalUnion;
        private String postcode;
        private String road;
        private String suburb;
    }

    @Data
    public static class Distance{
        private Integer meters;
    }
    @Data
    public static class Status{
        private Integer code;
        private String message;
    }

    @Data
    public static class StayInformed{
        private String blog;
        private String mastodon;
    }
    @Data
    public static class Timestamp{
        @JsonProperty("created_http")
        private String createdHttp;
        @JsonProperty("created_unix")
        private String createdUnix;
    }

}
