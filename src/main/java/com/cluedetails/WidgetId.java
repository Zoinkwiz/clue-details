package com.cluedetails;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class WidgetId {

    private int componentId;
    private int childIndex;

    public WidgetId(int componentId, int childIndex) {
        this.componentId = componentId;
        this.childIndex = childIndex;
    }
}
