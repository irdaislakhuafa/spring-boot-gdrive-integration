package com.irdaislakhuafa.dev.springbootgdriveintegration.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    private String icon;
    private String title;
    private String desc;
    private String url;
}
