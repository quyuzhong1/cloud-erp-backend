package com.sdk.oms.mercadolocal.dto.mercadolocal.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillViewDTO {


    @JsonProperty("site_id")
    private String siteId;
    @JsonProperty("fid")
    private String fid;
    @JsonProperty("buyer")
    private BuyerDTO buyer;
    @JsonProperty("seller")
    private SellerDTO seller;

    public String getSiteId() {
        if(siteId == null) {
            return "";
        }
        return siteId;
    }

    public String getFid() {
        if(fid == null) {
            return "";
        }
        return fid;
    }

    public BuyerDTO getBuyer() {
        if(buyer == null) {
            return new BuyerDTO();
        }
        return buyer;
    }

    public SellerDTO getSeller() {
        if(seller == null) {
            return new SellerDTO();
        }
        return seller;
    }

    @NoArgsConstructor
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BuyerDTO {
        public String getCustId() {
            if(custId == null) {
                return "";
            }
            return custId;
        }

        public BillingInfoDTO getBillingInfo() {
            if(billingInfo == null) {
                return new BillingInfoDTO();
            }
            return billingInfo;
        }

        @JsonProperty("cust_id")
        private String custId;
        @JsonProperty("billing_info")
        private BillingInfoDTO billingInfo;

        @NoArgsConstructor
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BillingInfoDTO {
            public String getName() {
                if(name == null) {
                    return "";
                }
                return name;
            }

            public String getLastName() {
                if(lastName == null) {
                    return "";
                }
                return lastName;
            }

            public IdentificationDTO getIdentification() {
                if(identification == null) {
                    return new IdentificationDTO();
                }
                return identification;
            }

            public TaxesDTO getTaxes() {
                if(taxes == null) {
                    return new TaxesDTO();
                }
                return taxes;
            }

            public AddressDTO getAddress() {
                if(address == null) {
                    return new AddressDTO();
                }
                return address;
            }

            public AttributesDTO getAttributes() {
                if(attributes == null) {
                    return new AttributesDTO();
                }
                return attributes;
            }

            @JsonProperty("name")
            private String name;
            @JsonProperty("last_name")
            private String lastName;
            @JsonProperty("identification")
            private IdentificationDTO identification;
            @JsonProperty("taxes")
            private TaxesDTO taxes;
            @JsonProperty("address")
            private AddressDTO address;
            @JsonProperty("attributes")
            private AttributesDTO attributes;

            @NoArgsConstructor
            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class IdentificationDTO {
                public String getType() {
                    if(type == null) {
                        return "";
                    }
                    return type;
                }

                public String getNumber() {
                    if(number == null) {
                        return "";
                    }
                    return number;
                }

                @JsonProperty("type")
                private String type;
                @JsonProperty("number")
                private String number;
            }

            @NoArgsConstructor
            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class TaxesDTO {
                public InscriptionsDTO getInscriptions() {
                    if(inscriptions == null) {
                        return new InscriptionsDTO();
                    }
                    return inscriptions;
                }

                public TaxpayerTypeDTO getTaxpayerType() {
                    if(taxpayerType == null) {
                        return new TaxpayerTypeDTO();
                    }
                    return taxpayerType;
                }

                @JsonProperty("inscriptions")
                private InscriptionsDTO inscriptions;
                @JsonProperty("taxpayer_type")
                private TaxpayerTypeDTO taxpayerType;

                @NoArgsConstructor
                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class InscriptionsDTO {
                    @JsonProperty("state_registration")
                    private String stateRegistration;
                }

                @NoArgsConstructor
                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class TaxpayerTypeDTO {
                }
            }

            @NoArgsConstructor
            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AddressDTO {
                @JsonProperty("street_name")
                private String streetName;
                @JsonProperty("street_number")
                private String streetNumber;
                @JsonProperty("city_name")
                private String cityName;
                @JsonProperty("comment")
                private String comment;
                @JsonProperty("neighborhood")
                private String neighborhood;
                @JsonProperty("state")
                private StateDTO state;
                @JsonProperty("zip_code")
                private String zipCode;
                @JsonProperty("country_id")
                private String countryId;

                @NoArgsConstructor
                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class StateDTO {
                    public String getCode() {
                        if(code == null) {
                            return "";
                        }
                        return code;
                    }

                    public String getName() {
                        if(name == null) {
                            return "";
                        }
                        return name;
                    }

                    @JsonProperty("code")
                    private String code;
                    @JsonProperty("name")
                    private String name;
                }
            }

            @NoArgsConstructor
            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AttributesDTO {
                public String getVatDiscriminatedBilling() {
                    if(vatDiscriminatedBilling == null) {
                        return "";
                    }
                    return vatDiscriminatedBilling;
                }

                public String getNormalized() {
                    if(normalized == null) {
                        return "";
                    }
                    return normalized;
                }

                public String getCustType() {
                    if(custType == null) {
                        return "";
                    }
                    return custType;
                }

                @JsonProperty("vat_discriminated_billing")
                private String vatDiscriminatedBilling;
                @JsonProperty("normalized")
                private String normalized;
                @JsonProperty("cust_type")
                private String custType;
            }
        }
    }

    @NoArgsConstructor
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SellerDTO {
        public String getCustId() {
            if(custId == null) {
                return "";
            }
            return custId;
        }

        @JsonProperty("cust_id")
        private String custId;
    }
}
