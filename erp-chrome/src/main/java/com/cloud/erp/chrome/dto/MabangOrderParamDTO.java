package com.cloud.erp.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname MabanOrderParamDTO
 * @Description TODO
 * @Date 2022-08-25 15:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MabangOrderParamDTO implements Serializable {


    private Integer formType=1;

    private String orderByField="";

    private String orderByValue="";
    private Integer historyType=1;
    private String keyColumnName="";
    private String keyColumnValue="";
    private String shippingType="shippingCostNew";
    private String paytimeTimeStart="";
    private String paytimeTimeEnd="";
    private String expresstimeTimeStart;
    private String expresstimeTimeEnd;
    private String searchTextValue="";
    private String searchTextType="platformOrderId";
    private String is_evaluation="";
    private String is_split="";
    private String is_union="";
    private String is_resend="";
    private String fba_flag="";
    private String searchTextTypeWhere="";
    private String moneyType="RMB";
    private String orderIds="";
    private String isAuth="";
    private Integer orderFlag=2;
    private String fbaFlag="";
    private Integer page=1;
    private Integer rowsPerPage=100;







}
