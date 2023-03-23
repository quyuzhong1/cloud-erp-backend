package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/23 12:25
 */
@Data
@NoArgsConstructor
public class ExcelImportDTO implements Serializable {

    private MultipartFile excelFile;

    private List<String> skuIds;

}
