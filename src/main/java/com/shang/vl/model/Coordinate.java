package com.shang.vl.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shangwei2009@hotmail.com on 2025/8/27 11:09
 */
@Data
@Accessors(chain = true)
public class Coordinate {

    @Description("X轴坐标值，精确到个位数，如：883")
    private Integer x;

    @Description("Y轴坐标值，精确到个位数，如：321")
    private Integer y;
}
