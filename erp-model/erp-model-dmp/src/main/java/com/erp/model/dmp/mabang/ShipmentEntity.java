package com.erp.model.dmp.mabang;

import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.mabang.item.ShipmentItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @CreateTime: 2023-06-29  16:05
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class ShipmentEntity extends CleanBaseDTO {

    private String _id;

    /**
     * 申报id
     */
    private String shippNo;

    /**
     * 批次
     */
    private String batchNo;

    /**
     * 申报名称
     */
    private String shippName;

    /**
     * 发件状态
     */
    private String ShipmentStatus;

    /**
     * 1:等待发货;2:已发货;3:已签收;4已删除
     */
    private String status;

    /**
     * 发货时间
     */
    private String expressTime;

    /**
     * 备注
     */
    private String content;

    /**
     * 发货仓库ID
     */
    private String warehouseId;

    /**
     * 发货仓库编码
     */
    private String warehouseCode;

    /**
     * 目的地仓库ID(fba仓库)
     */
    private String fbaWarehouseId;

    /**
     * 最近修改时间
     */
    private String timeLastModified;

    /**
     * 货件编号
     */
    private String shipmentId;

    /**
     * 明细列表
     */
    private List<ShipmentItemEntity> itemList;

    /**
     * 箱子信息
     */
    private Object boxInfo;

    /**
     * 费用信息
     */
    private Object headingcostdetail;

    /**
     * 是否完结1是2否
     */
    private String isOver;

    /**
     * 完结时间
     */
    private String overTime;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 始发港编码
     */
    private String startportCode;

    /**
     * 目的港编码
     */
    private String endportCode;


    @Override
    public String toString() {
        return "ShipmentEntity{" +
                "_id='" + _id + '\'' +
                ", shippNo='" + shippNo + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", shippName='" + shippName + '\'' +
                ", ShipmentStatus='" + ShipmentStatus + '\'' +
                ", status='" + status + '\'' +
                ", expressTime='" + expressTime + '\'' +
                ", content='" + content + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", fbaWarehouseId='" + fbaWarehouseId + '\'' +
                ", timeLastModified='" + timeLastModified + '\'' +
                ", shipmentId='" + shipmentId + '\'' +
                ", itemList=" + itemList +
                ", boxInfo=" + boxInfo +
                ", headingcostdetail=" + headingcostdetail +
                ", isOver='" + isOver + '\'' +
                ", overTime='" + overTime + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", startportCode='" + startportCode + '\'' +
                ", endportCode='" + endportCode + '\'' +
                '}';
    }
}