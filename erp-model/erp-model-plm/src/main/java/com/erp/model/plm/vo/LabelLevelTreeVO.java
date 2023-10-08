package com.erp.model.plm.vo;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zdy
 * @ClassName LabelLevelTreeVO
 * @description: 根据标签等级组装树结构
 * @date 2023年09月19日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
public class LabelLevelTreeVO {

    /**
     * 标签名称
     */
    private String name;
    /**
     * 颜色
     */
    private String color;
    /**
     * 标签级别 private 私有，company 公司
     */
    private String level;

    /**
     * 子集
     */
    private List<LabelBasicVO> children;

}
