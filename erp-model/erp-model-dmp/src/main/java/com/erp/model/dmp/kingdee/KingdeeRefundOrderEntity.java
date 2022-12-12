package com.erp.model.dmp.kingdee;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class KingdeeRefundOrderEntity {
    private String FID;
    private String FBillTypeID;
    private String FBillTypeName;
    private String FBillNo;
    private String FDATE;
    private String FSETTLERATE;
    private String FREFUNDAMOUNTFOR_H;
    private String FDOCUMENTSTATUS;
    private String FRECTUNIT;
    private String FRECTUNITName;
    private String FSETTLECURCode;
    private String FREALREFUNDAMOUNTFOR;
    private String FEXCHANGERATE;
    private String FWRITTENOFFSTATUS;
    private String FCancelStatus;
    private String FREMARK;
    private String FCreateDate;
    private String FModifyDate;
    private String FApproveDate;
    private String FWBSETTLENO;
    private String FCountry;
    private String FSALEORGName;
    private String FSALEORGID;
    private String FSALEERID;
    private String FSALEERName;
}
