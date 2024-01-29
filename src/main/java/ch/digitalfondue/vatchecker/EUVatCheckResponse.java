/*
 * Copyright © 2018-2024 digitalfondue (info@digitalfondue.ch)
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

public class EUVatCheckResponse {

    private final boolean isValid;
    private final String name;
    private final String address;

    private final boolean error;
    private final Fault fault;

    EUVatCheckResponse(boolean isValid, String name, String address, boolean error, Fault fault) {
        this.isValid = isValid;
        this.name = name;
        this.address = address;
        this.error = error;
        this.fault = fault;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isError() {
        return error;
    }

    public Fault getFault() {
        return fault;
    }

    public static class Fault extends BaseFault<FaultType> {
        Fault(String faultCode, String fault) {
            super(faultCode, fault, FaultType::valueOf, FaultType.OTHER);
        }
    }

    public enum FaultType {
        INVALID_INPUT,
        GLOBAL_MAX_CONCURRENT_REQ,
        MS_MAX_CONCURRENT_REQ,
        SERVICE_UNAVAILABLE,
        MS_UNAVAILABLE,
        TIMEOUT,
        OTHER
    }
}
