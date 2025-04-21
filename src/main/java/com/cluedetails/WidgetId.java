package com.cluedetails;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class WidgetId
{

    private final int componentId;
    private final Integer childIndex;

    public WidgetId(int componentId, Integer childIndex)
    {
        this.componentId = componentId;
        this.childIndex = childIndex;
    }
}
