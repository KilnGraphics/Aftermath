/*
 * MIT License
 *
 * Copyright (c) 2021 OroArmor (Eli Orona)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oroarmor.aftermath.callback;

import com.oroarmor.aftermath.AftermathCallbackCreationHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.LibFFI;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

import static org.lwjgl.system.MemoryUtil.memGetAddress;
import static org.lwjgl.system.MemoryUtil.memGetLong;
import static org.lwjgl.system.libffi.LibFFI.*;

public interface ShaderSourceDebugInfoLookupCallbackI extends CallbackI {
    FFICIF CIF = APIUtil.apiCreateCIF(
            LibFFI.FFI_DEFAULT_ABI,
            ffi_type_void,
            ffi_type_pointer, ffi_type_uint32, ffi_type_pointer
    );

    @Override
    @NotNull
    default FFICIF getCallInterface() {
        return CIF;
    }

    @Override
    default void callback(long ret, long args) {
        invoke(
                MemoryUtil.memUTF8(memGetLong(memGetAddress(args)), 128),
                AftermathCallbackCreationHelper.createSetShaderDebugInfo(memGetLong(memGetAddress(args + POINTER_SIZE))),
                memGetAddress(memGetAddress(args + 2L * POINTER_SIZE))
        );
    }

    void invoke(@NativeType("GFSDK_Aftermath_ShaderHash *") String shaderDebugName, @NativeType("PFN_GFSDK_Aftermath_SetData") BiFunction<ByteBuffer, Integer, Integer> setShaderBinary, @NativeType("void *") long pUserData);
}
