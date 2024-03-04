package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaleTermsBean {
    /**
     * id : WARRANTY_TIME
     * name : Warranty time
     * value_id : null
     * value_name : 1 months
     * value_struct : {"number":1,"unit":"months"}
     * values : [{"id":null,"name":"1 months","struct":{"number":1,"unit":"months"}}]
     * value_type : number_unit
     */

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("value_id")
    private Object valueId;
    @SerializedName("value_name")
    private String valueName;
    @SerializedName("value_struct")
    private ValueStructBean valueStruct;
    @SerializedName("value_type")
    private String valueType;
    @SerializedName("values")
    private List<ValuesBean> values;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValueId() {
        return valueId;
    }

    public void setValueId(Object valueId) {
        this.valueId = valueId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public ValueStructBean getValueStruct() {
        return valueStruct;
    }

    public void setValueStruct(ValueStructBean valueStruct) {
        this.valueStruct = valueStruct;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public List<ValuesBean> getValues() {
        return values;
    }

    public void setValues(List<ValuesBean> values) {
        this.values = values;
    }
}
