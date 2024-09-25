package com.magicvector.common.application.counter;

import java.util.List;

public interface CouterService {

    /**
     * 提报统计登记
     * @param group 提报到哪个“统计组”
     * @param index 提报到哪个“统计指标”
     * @param uniqueId 提报的唯一标识，如果为空，则不做唯一性识别，有1个就加1个
     */
    void counter(String group, String index, String uniqueId);


    Long getCounterValue(String group, String index);


    List<String> getCounterDetail(String group, String index);

    void cleanCounter(String group, String index);

    void cleanGroup(String group);

}
