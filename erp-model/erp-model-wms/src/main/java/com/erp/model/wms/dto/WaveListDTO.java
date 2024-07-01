package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 波次拣货DTO
 * @date 2024-06-20
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class WaveListDTO implements Serializable {

    @Data
    public static class SearchParamDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 波次编码
         */
        private String waveCode;

        /**
         * 波次名称
         */
        private String waveName;

        /**
         * 波次类型
         */
        private String waveTypeCode;

        /**
         * 拣货车类型
         */
        private String pickingTruckType;

        /**
         * 拣货车编码
         */
        private String pickingTruckCode;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 波次状态
         */
        private String waveStatusCode;

        /**
         * 打印状态
         */
        private String printStatus;

        /**
         * 是否缺货
         */
        private String isSoldOut;

        /**
         * 创建人
         */
        private String createUser;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 拣货人
         */
        private String pickingUser;

        /**
         * 拣货时间
         */
        private String pickingTime;
    }

    @Data
    public static class ViewDTO {
        /**
         * 波次ID
         */
        private String id;
        /**
         * 波次编码
         */
        private String code;

        /**
         * 波次名称
         */
        private String name;

        /**
         * 波次类型
         */
        private String type;

        /**
         * 拣货车类型
         */
        private String pickingCartType;

        /**
         * 拣货车编码
         */
        private String pickingCartCode;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 波次状态
         */
        private String status;

        /**
         * 打印状态
         */
        private String printStatus;

        /**
         * 是否缺货
         */
        private Boolean isOutStock;

        /**
         * 异常原因
         */
        private String exceptionCause;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 拣货人
         */
        private String pickingUserName;

        /**
         * 拣货时间
         */
        private LocalDateTime pickingTime;

        /**
         * 完成时间
         */
        private LocalDateTime finishTime;

        /**
         * 打印时间
         */
        private LocalDateTime printTime;
    }

    @Data
    public static class SkuInfoDTO{
        private String skuId;

        private String skuNo;

        /**
         * 销售数量
         */
        private Integer salesQty;

        /**
         * 已拣数量汇总
         */
        private Integer pickedSumQty;

        /**
         * 拣货仓位信息
         */
        private List<PickingLocationInfoDTO> pickingLocationInfoList;
    }

    @Data
    public static class DeliveryInfoDTO {
        /**
         * 销售订单编号
         */
        private String soCode;

        /**
         * 发货单号
         */
        private String soB2cDeliveryCode;

        /**
         * 拣货状态
         */
        private String pickingStatus;

        /**
         * 物流渠道
         */
        private String logisticsChannelName;

        private List<SkuInfoDTO> skuInfoList;
    }

    @Data
    public static class PickingLocationInfoDTO {
        /**
         * 库区
         */
        private String warehouseArea;

        /**
         * 库区名称
         */
        private String warehouseAreaName;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 是否缺货
         */
        private String isOutStock;

        /**
         * 应拣数量
         */
        private Integer shouldPickQty;

        /**
         * 已拣数量
         */
        private Integer pickedQty;
    }

    @Data
    public static class TabDTO{
        /**
         * tab页代码<br/>
         * await_pick：待拣货<br/>
         * pick_ing：拣货中<br/>
         * hang_up：挂起<br/>
         * finish：已完成
         */
        private String tabCode;

        /**
         * 统计数量
         */
        private  Integer count;
    }
}
