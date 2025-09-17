package com.weinhold.constellation.creator.files;

import java.io.File;

import com.weinhold.constellation.creator.files.model.FileCreationInput;

public interface FileCreator {

    /**
     * Creates a file based on the provided input.
     *
     * @param input the input containing necessary data for file creation
     * @return the created file
     */
    File createFile(FileCreationInput input);
}
