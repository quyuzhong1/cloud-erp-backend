package com.erp.server.tms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.tms.enums.TransferDeclareTabFlagEnum;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 中转报关表
 */
@Component
public class TmsTransferDeclareQueryHandler extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if(field.equals("tab")){
            if(TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode().equals(value.toString())){
                //待上传
                this.buildDefaultDTO("td.instock_forecast_status",TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode());
            }else if(TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode().equals(value.toString())){
                //上传失败
                this.buildDefaultDTO("td.instock_forecast_status",TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode());
            }else if (TransferDeclareTabFlagEnum.LOGISTICS_UN_OUTSTOCK.getCode().equals(value.toString())){
                //物流商未出库
                List<String> transferStatusList = new ArrayList<>(5);
                transferStatusList.add(TransferLogisticsStatusEnum.DELETED.getCode());
                transferStatusList.add(TransferLogisticsStatusEnum.DRAFT.getCode());
                transferStatusList.add(TransferLogisticsStatusEnum.UNUSUAL.getCode());
                transferStatusList.add(TransferLogisticsStatusEnum.CONFIRMED.getCode());
                transferStatusList.add(TransferLogisticsStatusEnum.SUBMITTED.getCode());
                this.buildSplicingSQLDTO("tdd.transfer_status", QueryConditionEnum.IN_LIST,transferStatusList, QueryDataTypeEnum.STRING);
            }else if (TransferDeclareTabFlagEnum.LOGISTICS_OUTSTOCK.getCode().equals(value.toString())){
                //物流商已出库
                List<String> transferStatusList = new ArrayList<>(2);
                transferStatusList.add(TransferLogisticsStatusEnum.OUTSTOCK.getCode());
                transferStatusList.add(TransferLogisticsStatusEnum.SIGNED.getCode());
                this.buildSplicingSQLDTO("tdd.transfer_status", QueryConditionEnum.IN_LIST,transferStatusList, QueryDataTypeEnum.STRING);
            }
        }

        if(field.equals("tdd.outstockStatus")){
            if((Boolean)value){
                this.buildDefaultDTO("sb.bill_status", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            }else{
                this.buildSplicingSQLDTO("sb.bill_status", QueryConditionEnum.NE,SoB2cBillStatusEnum.ENUM_SHIPPED.getCode(), QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}

