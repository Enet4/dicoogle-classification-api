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

import java.util.Objects;
import java.util.function.Supplier;

/** Simple utility class for a lazy-initialized object that can be cleared on-demand.
 * This implementation is not thread-safe. Concurrent access might result in the supplier function being called
 * more than once.
 *
 * @author Eduardo Pinho
 */
public class LazyLoader<T> {
    private volatile T instance;
    private final Supplier<T> init;

    public LazyLoader(Supplier<T> init) {
        Objects.requireNonNull(init);
        this.init = init;
    }

    public T get() {
        if (this.instance == null) {
            this.instance = this.init.get();
        }
        return this.instance;
    }

    public void reset() {
        this.instance = null;
    }

    public boolean isLoaded() {
        return this.instance != null;
    }
}
