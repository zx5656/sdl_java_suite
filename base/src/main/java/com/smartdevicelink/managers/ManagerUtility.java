package com.smartdevicelink.managers;

import com.smartdevicelink.proxy.rpc.ImageField;
import com.smartdevicelink.proxy.rpc.TextField;
import com.smartdevicelink.proxy.rpc.WindowCapability;
import com.smartdevicelink.proxy.rpc.enums.ImageFieldName;
import com.smartdevicelink.proxy.rpc.enums.TextFieldName;

/**
 * <strong>ManagerUtility</strong> <br>
 * <p>
 * Static Methods to be used throughout the Manager classes <br>
 */
public class ManagerUtility {

    public static class WindowCapabilityUtility {

        /**
         * Check to see if WindowCapability has an ImageFieldName of a given name.
         *
         * @param windowCapability WindowCapability representing the capabilities of the desired window
         * @param name ImageFieldName representing a name of a given Image field that would be stored in WindowCapability
         * @return true if name exist in WindowCapability else false
         */
        public static boolean hasImageFieldOfName(WindowCapability windowCapability, ImageFieldName name) {
            if (windowCapability == null || name == null) {
                return false;
            }
            if (windowCapability.getImageFields() != null) {
                for (ImageField field : windowCapability.getImageFields()) {
                    if (field != null && name.equals(field.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Check to see if WindowCapability has a textField of a given name.
         *
         * @param windowCapability WindowCapability representing the capabilities of the desired window
         * @param name TextFieldName representing a name of a given text field that would be stored in WindowCapability
         * @return true if name exist in WindowCapability else false
         */
        public static boolean hasTextFieldOfName(WindowCapability windowCapability, TextFieldName name) {
            if (windowCapability == null || name == null) {
                return false;
            }
            if (windowCapability.getTextFields() != null) {
                for (TextField field : windowCapability.getTextFields()) {
                    if (field != null && name.equals(field.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Method to get number of textFields allowed to be set according to WindowCapability
         *
         * @param windowCapability WindowCapability representing the capabilities of the desired window
         * @return linesFound Number of textFields found in WindowCapability
         */
        public static int getMaxNumberOfMainFieldLines(WindowCapability windowCapability) {
            int highestFound = 0;
            if (windowCapability != null && windowCapability.getTextFields() != null) {
                for (TextField field : windowCapability.getTextFields()) {
                    int fieldNumber = 0;
                    switch (field.getName()) {
                        case mainField1:
                            fieldNumber = 1;
                            break;
                        case mainField2:
                            fieldNumber = 2;
                            break;
                        case mainField3:
                            fieldNumber = 3;
                            break;
                        case mainField4:
                            fieldNumber = 4;
                            break;
                    }
                    if (fieldNumber > 0) {
                        highestFound = Math.max(highestFound, fieldNumber);
                        if (highestFound == 4) {
                            break;
                        }
                    }
                }
            }
            return highestFound;
        }
    }
}
