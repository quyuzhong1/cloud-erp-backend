package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname MyDashboardDTO
 * @Description TODO
 * @Date 2022-12-09 9:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MyDashboardDTO  implements Serializable {

    /**
     * 常用仪表盘
     */
    private List<DashboardDTO> frequentlyList;


    /**
     * 我创建的仪表盘
     */
    private List<DashboardDTO> myCreateList;


    /**
     * 其它仪表盘
     */
    private List<DashboardDTO> otherList;
}
