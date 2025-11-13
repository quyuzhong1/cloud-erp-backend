package com.erp.model.wms.dto.third;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseUploadOrderLabelReq extends ThirdWarehouseAuth{
    //订单号
    @NotBlank(message = "订单号不能为空")
    private String orderCode;
    //物流跟踪号
    @NotBlank(message = "物流跟踪号不能为空")
    private String trackNo;

    //货主编码
    private String ownerCode;
    //文件类型
    private String fileType;
    //面单URL数组
    private List<String> fileUrlList;
    //附件ID列表
    private List<Integer> fileIdList;
}
