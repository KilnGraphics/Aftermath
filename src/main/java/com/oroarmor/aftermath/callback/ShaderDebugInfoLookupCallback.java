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

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.Callback;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

import static org.lwjgl.system.MemoryUtil.NULL;


public abstract class ShaderDebugInfoLookupCallback extends Callback implements ShaderDebugInfoLookupCallbackI {
    public static ShaderDebugInfoLookupCallback create(long functionPointer) {
        ShaderDebugInfoLookupCallbackI instance = Callback.get(functionPointer);
        return instance instanceof ShaderDebugInfoLookupCallback
                ? (ShaderDebugInfoLookupCallback) instance
                : new Container(functionPointer, instance);
    }

    @Nullable
    public static ShaderDebugInfoLookupCallback createSafe(long functionPointer) {
        return functionPointer == NULL ? null : create(functionPointer);
    }

    public static ShaderDebugInfoLookupCallback create(ShaderDebugInfoLookupCallbackI instance) {
        return instance instanceof ShaderDebugInfoLookupCallback
                ? (ShaderDebugInfoLookupCallback)instance
                : new Container(instance.address(), instance);
    }

    protected ShaderDebugInfoLookupCallback() {
        super(CIF);
    }

    ShaderDebugInfoLookupCallback(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container extends ShaderDebugInfoLookupCallback {
        private final ShaderDebugInfoLookupCallbackI delegate;

        Container(long functionPointer, ShaderDebugInfoLookupCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }


        @Override
        public void invoke(long[] pIdentifier, BiFunction<ByteBuffer, Integer, Integer> setShaderDebugInfo, long pUserData) {
            delegate.invoke(pIdentifier, setShaderDebugInfo, pUserData);
        }
    }
}
