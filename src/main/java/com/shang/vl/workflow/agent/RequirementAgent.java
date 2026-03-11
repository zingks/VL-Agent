package com.shang.vl.workflow.agent;

import java.util.List;

/**
 * 需求生成 Agent。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:24
 */
public interface RequirementAgent {

    String generateRequirementFromImages(List<String> imageUrls, String originalDemand);
}
