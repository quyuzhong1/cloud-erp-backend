package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**  变更排期导入接口
 * @Classname
 * @Description TODO
 * @Date 2023-02-14 14:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChangeScheduleExportResultVO  implements Serializable {

    /**
     * 成功的数据
     */
    private List<ChangeScheduleExportVO> succeedList;


    /**
     * 失败的链接
     */
    private String errorUrl;
}
