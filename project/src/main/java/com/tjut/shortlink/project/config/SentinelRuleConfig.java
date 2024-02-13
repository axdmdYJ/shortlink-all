package com.tjut.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

    /**
     * 在 Bean 初始化完成后执行的方法，用于初始化 Sentinel 流控规则。
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 创建一个空的流控规则列表
        List<FlowRule> rules = new ArrayList<>();

        // 创建一个针对 "create_short-link" 资源的流控规则
        FlowRule createOrderRule = new FlowRule();
        createOrderRule.setResource("create_short-link"); // 设置资源名称
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 设置流控模式为 QPS
        createOrderRule.setCount(1); // 设置允许通过的请求数量，这里设置为每秒允许通过 1 个请求
        rules.add(createOrderRule); // 将规则添加到列表

        // 载入 Sentinel 流控规则
        FlowRuleManager.loadRules(rules);
    }
}