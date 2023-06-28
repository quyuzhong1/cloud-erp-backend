package com.erp.model.dmp.dto.mabang;

import lombok.Data;

import java.io.Serializable;

/**
 * @CreateTime: 2023-06-28  14:39
 * @Author: zhangchunlin
 */
@Data
public class DmpMabangInOutStockMsgDTO implements Serializable {

    /**
     * dmp出入库数据id
     */
    private String dmpOutInStockId;

    /**
     * 需要发送到马帮的出入库数据集
     */
    private MabangInOutStockDTO mabangInOutStock;




}