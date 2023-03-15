package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商资质信息
 * @author Lambda
 * @Classname SupplierAptitudesDTO
 * @Description TODO
 * @Date 2023-03-15 17:29
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierAptitudesDTO implements Serializable {

    /**
     * 表id
     */
    private String id;


    /**
     * 有效开始时间
     */
    private LocalDate validStartTime;


    /**
     * 有效结束时间
     */
    private LocalDate validEndTime;

    /**
     * 备注
     */
    @Size(max = 255,message = "最大255字符")
    private String remark;


    /**
     * 资质附件url
     */
    private List<String> aptitudesAttachmentList;

}
