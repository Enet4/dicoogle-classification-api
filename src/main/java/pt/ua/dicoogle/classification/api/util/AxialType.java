/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle-classification-api.
 *
 * Dicoogle/dicoogle-classification-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle-classification-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.classification.api.util;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public enum AxialType {
    TRANSVERSE, CORONAL, SAGITTAL, OBLIQUE;

    private static final float[] TRANSVERSE_VECTOR = {1,0,0, 0,1,0};
    private static final float[] SAGITTAL_VECTOR = {0,1,0, 0,0,1};
    private static final float[] CORONAL_VECTOR = {1,0,0, 0,0,1};

    /** Obtain the kind of axial image of a CT DICOM object.
     *
     * @param obj the DICOM object, assumed to be CT
     * @return "transverse", "coronal", "sagittal", "oblique". empty if ImageType value #3 is not AXIAL
     */
    public static Optional<AxialType> getCTAxialType(DicomObject obj) {
        Objects.requireNonNull(obj);
        DicomElement eImageType = obj.get(Tag.ImageType);
        if (eImageType == null) {
            return Optional.empty();
        }
        String[] imageType = eImageType.getStrings(new SpecificCharacterSet("ISO_IR 100"), false);
        if (imageType == null || !Arrays.asList(imageType).contains("AXIAL")) {
            return Optional.empty();
        }

        DicomElement eImageOrientationPatient = obj.get(Tag.ImageOrientationPatient);
        float[] orientation = eImageOrientationPatient.getFloats(false);

        // test orientations
        if (arrayAbsEquals(orientation, TRANSVERSE_VECTOR, 1e-8f)) {
            return Optional.of(AxialType.TRANSVERSE);
        }
        if (arrayAbsEquals(orientation, CORONAL_VECTOR, 1e-8f)) {
            return Optional.of(AxialType.CORONAL);
        }
        if (arrayAbsEquals(orientation, SAGITTAL_VECTOR, 1e-8f)) {
            return Optional.of(AxialType.SAGITTAL);
        }
        return Optional.of(AxialType.OBLIQUE);
    }

    private static boolean arrayAbsEquals(float[] f1, float[] f2, float delta) {
        if (f1.length != f2.length) return false;
        for (int i = 0; i < f1.length; i++) {
            if (Math.abs(Math.abs(f1[i]) - Math.abs(f2[i])) > delta) {
                return false;
            }
        }
        return true;
    }
}
