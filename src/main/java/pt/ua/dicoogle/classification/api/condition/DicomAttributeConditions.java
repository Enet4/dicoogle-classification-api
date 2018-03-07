/**
 * Copyright (C) 2016  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
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
package pt.ua.dicoogle.classification.api.condition;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;

import pt.ua.dicoogle.sdk.utils.TagValue;
import pt.ua.dicoogle.sdk.utils.TagsStruct;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class DicomAttributeConditions implements DicomCondition {
    private final String tag;
    private final Predicate<DicomElement> pred;

    public DicomAttributeConditions(String tag, Predicate<DicomElement> pred) {
        this.tag = tag;
        this.pred = pred;
    }

    public static DicomAttributeConditions matches(String tag, String regex) {
        return new DicomAttributeConditions(tag, e -> {
            return e != null && Pattern.compile(regex).matcher(new String(e.getBytes()).trim()).matches();
        });
    }

    public static DicomAttributeConditions matches(String tag, Pattern pattern) {
        return new DicomAttributeConditions(tag, e -> {
            return e != null && pattern.matcher(new String(e.getBytes()).trim()).matches();
        });
    }

    public static DicomAttributeConditions equals(String tag, String value) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(value);
        return new DicomAttributeConditions(tag, e -> {
            return e != null && new String(e.getBytes()).trim().equals(value.trim());
        });
    }

    public static DicomAttributeConditions equals(String tag, int value) {
        Objects.requireNonNull(tag);
        return new DicomAttributeConditions(tag, e -> {
            return e != null && e.getInt(false) == value;
        });
    }

    public static DicomAttributeConditions exists(String tag) {
        return new DicomAttributeConditions(tag, e -> {
            return e != null && !e.isEmpty();
        });
    }

    @Override
    public boolean test(DicomObject obj) {
        TagsStruct tags = TagsStruct.getInstance(); // global instance
        TagValue val = tags.getTagValue(tag);
        DicomElement e = obj.get(val.getTagNumber());
        return this.pred.test(e);
    }
}
