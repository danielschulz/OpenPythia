/**
 * Copyright 2012 msg systems ag
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.openpythia.utilities;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class FileSelectorUtility {

    private FileSelectorUtility() {
    }

    /**
     * Get the extension of a file.
     * 
     * @param file
     *            File for which the extension should be returned.
     * @return The extension of the file, null if there is no.
     */
    public static String getExtension(File file) {
        String ext = null;
        String fileName = file.getName();
        int indexExtension = fileName.lastIndexOf('.');

        if (indexExtension > 0 && indexExtension < fileName.length() - 1) {
            ext = fileName.substring(indexExtension + 1).toLowerCase();
        }
        return ext;
    }

    public static File chooseExcelFileToWrite(Component owner, File pathToNavigateTo) {
        return chooseFileToWrite(owner, new FileFilterExcel(), ".xlsx", pathToNavigateTo, null);
    }

    public static File chooseSQLFileToWrite(Component owner, File pathToNavigateTo, String fileName) {
        return chooseFileToWrite(owner, new FileFilterSQL(), ".sql", pathToNavigateTo, fileName);
    }

    public static File chooseSnapshotFileToWrite(Component owner, File pathToNavigateTo, String snapshotIdToSave) {
        String fileNameSuggestion = suggestedFileNameForSnapshotID(snapshotIdToSave);

        return chooseFileToWrite(owner, new FileFilterSnapshot(), ".snap", pathToNavigateTo, fileNameSuggestion);
    }

    public static String suggestedFileNameForSnapshotID(String snapshotId) {
        return  snapshotId.replaceAll(" ", "_")
                .replaceAll("\\.", "")
                .replaceAll(":", "") + ".snap";
    }

    private static File chooseFileToWrite(Component owner, FileFilter filter, String extension, File pathToNavigateTo, String fileName) {

        File result = null;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        if (pathToNavigateTo != null) {
            fileChooser.setCurrentDirectory(pathToNavigateTo);
        }
        if (fileName != null) {
            fileChooser.setSelectedFile(new File(fileName));
        }

        int returnVal = fileChooser.showSaveDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            result = fileChooser.getSelectedFile();

            if (FileSelectorUtility.getExtension(result) == null) {
                result = new File(result.getAbsolutePath() + extension);
            }

            if (result != null && result.exists()) {
                int answer = JOptionPane.showConfirmDialog(owner,
                        "This file already exists. Overwrite?", "File exists",
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    result = null;
                }
            }
        }
        return result;
    }

    public static File chooseJarFileToRead() {
        return chooseFileToRead(null, new FileFilterJar(), null);
    }

    public static File chooseSnapshotFileToRead(Component owner, File pathToNavigateTo) {
        return chooseFileToRead(owner, new FileFilterSnapshot(), pathToNavigateTo);
    }

    private static File chooseFileToRead(Component owner, FileFilter filter, File pathToNavigateTo) {

        File result = null;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        if (pathToNavigateTo != null) {
            fileChooser.setCurrentDirectory(pathToNavigateTo);
        }

        int returnVal = fileChooser.showOpenDialog(owner);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            result = fileChooser.getSelectedFile();
        }
        return result;
    }
}
