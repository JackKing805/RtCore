/*
 * Decompiled with CFR 0.150.
 */
package com.jerry.rt.core.http.request.interfaces;

import java.net.Socket;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(mv={1, 7, 1}, k=1, xi=48, d1={"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\b"}, d2={"Lcom/jerry/rt/core/http/request/interfaces/SocketListener;", "", "onSocketIn", "", "socket", "Ljava/net/Socket;", "(Ljava/net/Socket;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "onSocketOut", "RtCore"})
public interface SocketListener {
    @Nullable
    public Object onSocketIn(@NotNull Socket var1, @NotNull Continuation<? super Unit> var2) throws Exception;

    @Nullable
    public Object onSocketOut(@NotNull Socket var1, @NotNull Continuation<? super Unit> var2) throws Exception;
}
