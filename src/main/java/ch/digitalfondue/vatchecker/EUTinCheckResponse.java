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

public class EUTinCheckResponse {

    private final boolean validSyntax;
    private final boolean validStructure;

    private final boolean error;
    private final Fault fault;

    EUTinCheckResponse(boolean validSyntax, boolean validStructure, boolean error, Fault fault) {
        this.validSyntax = validSyntax;
        this.validStructure = validStructure;
        this.error = error;
        this.fault = fault;
    }

    public boolean isValidSyntax() {
        return validSyntax;
    }

    public boolean isValidStructure() {
        return validStructure;
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
        NO_INFORMATION,
        SERVICE_UNAVAILABLE,
        SERVER_BUSY,
        OTHER
    }
}
