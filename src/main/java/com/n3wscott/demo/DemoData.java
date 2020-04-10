package com.n3wscott.demo;

import lombok.Getter;
import lombok.Setter;

public class DemoData {
    @Getter @Setter private String data;

    @Override
    public String toString() {
        return "DemoData{" +
                "data='" + data + '\'' +
                '}';
    }
}
