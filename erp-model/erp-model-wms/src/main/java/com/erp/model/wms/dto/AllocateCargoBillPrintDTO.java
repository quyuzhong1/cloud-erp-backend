package com.erp.model.wms.dto;

import com.erp.model.wms.entity.PickingWaveEntity;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 配货单打印dto
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@NoArgsConstructor
public class AllocateCargoBillPrintDTO implements Serializable {


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WaveDTO {
        /**
         * 波次id
         */
        private String waveId;
        /**
         * 波次号
         */
        private String code;

        public static WaveDTO convertFromPickingWaveEntity(PickingWaveEntity pickingWaveEntity){
            if(Objects.isNull(pickingWaveEntity)){
                return new WaveDTO();
            }
            return new WaveDTO(pickingWaveEntity.getId(),pickingWaveEntity.getCode());
        }

    }


    @Data
    @Builder
    public static class ScanWaveDTO {

        /**
         * 是否直接打印 当扫描波次号或者拣货车有对应的拣货中的波次 时为true,前端直接调打印接口
         */
        private Boolean isDirectPrint;

        /**
         * 波次号 如果isDirectPrint为true 这个数组只会有一个值
         */
        private List<WaveDTO> waveList;
    }
}