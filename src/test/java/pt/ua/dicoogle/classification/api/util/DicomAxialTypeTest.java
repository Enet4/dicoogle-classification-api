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

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class DicomAxialTypeTest {

    @Test
    public void testFail() throws IOException {
        BasicDicomObject obj = new BasicDicomObject();
        obj.putString(Tag.Modality, VR.CS, "CR");
        Assert.assertEquals(Optional.empty(), AxialType.getCTAxialType(obj));
    }

    @Test
    public void testOk() throws IOException {
        BasicDicomObject obj = new BasicDicomObject();
        obj.putString(Tag.Modality, VR.CS, "CR");
        obj.putString(Tag.ImageType, VR.CS, "ORIGINAL\\PRIMARY\\AXIAL");
        obj.putDoubles(Tag.ImageOrientationPatient, VR.DS, new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0});
        Assert.assertEquals(Optional.of(AxialType.TRANSVERSE), AxialType.getCTAxialType(obj));
    }
}
