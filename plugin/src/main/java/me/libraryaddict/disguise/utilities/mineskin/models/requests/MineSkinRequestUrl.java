package me.libraryaddict.disguise.utilities.mineskin.models.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class MineSkinRequestUrl extends MineSkinSubmitQueue {
    private String url;
}
