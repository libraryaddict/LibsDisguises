package me.libraryaddict.disguise.utilities.mineskin.models.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;

@Getter
@Setter
@Accessors(chain = true)
public class MineSkinRequestFile extends MineSkinSubmitQueue {
    // transient as we do not actually want the file sent
    private transient File file;
}
