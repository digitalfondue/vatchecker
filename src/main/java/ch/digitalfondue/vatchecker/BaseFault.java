/*
 *
 * Copyright © 2018-2021 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.vatchecker;

import java.util.function.Function;

abstract class BaseFault<T extends Enum<T>> {

    private final String faultCode;
    private final String fault;
    private final T faultType;

    protected BaseFault(String faultCode, String fault, Function<String, T> converter, T defaultValue) {
        this.faultCode = faultCode;
        this.fault = fault;
        this.faultType = tryParse(fault, converter, defaultValue);
    }

    public String getFault() {
        return fault;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public T getFaultType() {
        return faultType;
    }

    private static <T extends Enum<T>> T tryParse(String fault, Function<String, T> converter, T defaultValue) {
        try {
            return converter.apply(fault);
        } catch (IllegalArgumentException | NullPointerException e) {
            return defaultValue;
        }
    }
}
