package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WaveListDTO implements Serializable {
    @Data
    public static class AddDTO {
        /**
         * 波次名称
         */
        private String name;

        /**
         * 发货单号集合
         */
        private List<String> deliveryIdList;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 拣货车类型
         */
        private List<String> pickCartTypeIdList;

        /**
         * 波次类型
         */
        private String waveType;
    }


    @Getter
    @Setter
    public static class PickingWaveDetailDTO {

        /**
         * 波次id
         */
        private String mainId;
        /**
         * 篮号
         */
        private String basketNo;

        /**
         * 发货id
         */
        private String deliveryId;
        /**
         * 拣货明细id
         */
        private String pickDetailId;
        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;
        /**
         * 已拣货数量
         */
        private Integer pickedQty;
        /**
         * 已分货数量
         */
        private Integer allocatedQty;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SearchParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
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
         * 波次类型名称
         */
        private String typeName;

        /**
         * 拣货车类型
         */
        private String pickingCartTypeName;

        /**
         * 拣货车编码
         */
        private String pickingCartCode;

        /**
         * 分拣方式名称
         */
        private String pickingTypeName;

        /**
         * 分拣方式
         */
        private String pickingType;

        /**
         * 波次状态：await_pick待拣货，pick_ing拣货中，hang_up挂起，finish已完成
         */
        private String status;

        /**
         * 波次状态名称
         */
        private String statusName;

        /**
         * 打印状态
         */
        private String printStatusName;

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
    @NoArgsConstructor
    public static class TabDTO{
        /**
         * tab页代码
         * await_pick：待拣货
         * pick_ing：拣货中
         * hang_up：挂起
         * finish：已完成
         */
        private String tabFlag;

        /**
         * tab页名称
         */
        private String tabFlagName;

        /**
         * 统计数量
         */
        private Integer count;

        public TabDTO(String tabFlag, Integer count){
            this.tabFlag = tabFlag;
            this.count = count;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ExportParamDTO extends SearchParamDTO{

    }


    @Getter
    @Setter
    public static class WaveDeliveryDTO {

        private String waveCode;

        private String deliveryId;

    }

    @Getter
    @Setter
    public static class WaveDeliveryStatusDTO {

        private String id;

        private String deliveryId;

        private String status;

    }
}
