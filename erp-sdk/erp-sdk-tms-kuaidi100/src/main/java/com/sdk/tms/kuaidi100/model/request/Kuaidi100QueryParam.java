package com.sdk.tms.kuaidi100.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 功能描述：快递100实时查询内部参数对象
 *
 * @author jack
 * @date 2026-03-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kuaidi100QueryParam implements Serializable {

    /**
     * 查询的快递公司的编码，一律用小写字母
     */
    private String com;

    /**
     * 查询的快递单号，单号的最小长度6个字符，最大长度32个字符
     */
    private String num;

    /**
     * 收、寄件人的电话号码（手机和固定电话均可）
     */
    private String phone;

    /**
     * 出发地信息，建议填写完整的省市区格式
     */
    private String from;

    /**
     * 目的地信息，建议填写完整的省市区格式
     */
    private String to;

    /**
     * 添加此字段表示开通行政区域解析功能以及物流轨迹增加物流状态名称
     * 0: 关闭（默认），1: 开通行政区域解析功能以及物流轨迹增加物流状态名称
     * 4: 开通行政解析功能以及物流轨迹增加物流高级状态名称、状态值并且返回出发、目的及当前城市信息
     */
    private String resultv2;

    /**
     * 返回格式：0：json（默认），1：xml，2：html，3：text
     */
    @Builder.Default
    private String show = "0";

    /**
     * 返回结果排序：desc：降序（默认），asc：升序
     */
    @Builder.Default
    private String order = "desc";

    /**
     * 返回结果语言版本：zh：中文（默认），en：英文
     */
    @Builder.Default
    private String lang = "zh";
}
