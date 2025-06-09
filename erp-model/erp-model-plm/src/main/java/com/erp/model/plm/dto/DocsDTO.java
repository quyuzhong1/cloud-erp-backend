package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname DocsDTO

 * @Date 2022-09-15 9:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DocsDTO implements Serializable {

    /**
     * 表id
     *
     * @author yl
     * @date 2022-10-09 10:51
     */
    private String id;

    /**
     * 文档名
     *
     * @author
     * @date 2022-10-09 10:51
     */
    @NotBlank(message = "文档名不能为空")
    private String name;


    /**
     * 1 启用 0 禁用
     */
    private boolean state;


    @Data
    @NoArgsConstructor
    public static class DeliveryDocsPowerDTO {


        private List<String> containDocsPowerList;

        private List<String> noContainDocsPowerList;
    }


}
