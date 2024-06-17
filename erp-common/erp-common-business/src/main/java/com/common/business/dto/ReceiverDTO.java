package com.common.business.dto;

/**
 * 收货人DTO接口，用来统一不同业务的收货人DTO，以简化代码
 */
public interface ReceiverDTO {
    String getProvince();

    void setProvince(String province);

    String getCity();

    void setCity(String city);

    String getAddressFirst();

    void setAddressFirst(String addressFirst);

    String getTelNumber();

    void setTelNumber(String telNumber);

    String getZipCode();

    void setZipCode(String zipCode);

    /**
     * 联系人姓名
     * @return
     */
    String getContact();

    void setContact(String contact);

    /**
     * 买家姓名
     * @return
     */
    String getBuyerName();

    void setBuyerName(String buyerName);
}
