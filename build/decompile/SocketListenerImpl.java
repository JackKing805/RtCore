/*
 * Decompiled with CFR 0.150.
 */
package com.jerry.rt.core.http.request.interfaces;

import com.jerry.rt.core.http.request.interfaces.SocketListener;
import com.jerry.rt.core.http.request.interfaces.SocketListenerImpl;
import com.jerry.rt.extensions.LogExtensionsKt;
import java.net.Socket;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlinx.coroutines.DelayKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Exception performing whole class analysis ignored.
 */
@Metadata(mv={1, 7, 1}, k=1, xi=48, d1={"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0016\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0019\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0007J\u0019\u0010\b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0007\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\t"}, d2={"Lcom/jerry/rt/core/http/request/interfaces/SocketListenerImpl;", "Lcom/jerry/rt/core/http/request/interfaces/SocketListener;", "()V", "onSocketIn", "", "socket", "Ljava/net/Socket;", "(Ljava/net/Socket;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "onSocketOut", "RtCore"})
public class SocketListenerImpl
implements SocketListener {
    @Override
    @Nullable
    public Object onSocketIn(@NotNull Socket socket, @NotNull Continuation<? super Unit> $completion) {
        return SocketListenerImpl.onSocketIn$suspendImpl(this, socket, $completion);
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    static /* synthetic */ Object onSocketIn$suspendImpl(SocketListenerImpl var0, Socket var1_1, Continuation<? super Unit> var2_2) {
        if (!(var2_2 instanceof onSocketIn.1)) ** GOTO lbl-1000
        var4_3 = var2_2;
        if ((var4_3.label & -2147483648) != 0) {
            var4_3.label -= -2147483648;
        } else lbl-1000:
        // 2 sources

        {
            $continuation = new ContinuationImpl(var0, var2_2){
                /* synthetic */ Object result;
                final /* synthetic */ SocketListenerImpl this$0;
                int label;
                {
                    this.this$0 = this$0;
                    super($completion);
                }

                @Nullable
                public final Object invokeSuspend(@NotNull Object $result) {
                    this.result = $result;
                    this.label |= Integer.MIN_VALUE;
                    return SocketListenerImpl.onSocketIn$suspendImpl(this.this$0, null, this);
                }
            };
        }
        $result = $continuation.result;
        var5_5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch ($continuation.label) {
            case 0: {
                ResultKt.throwOnFailure($result);
                LogExtensionsKt.logError("onSocketIn");
                ** GOTO lbl18
            }
            case 1: {
                ResultKt.throwOnFailure($result);
                v0 = $result;
lbl18:
                // 2 sources

                do {
                    $continuation.label = 1;
                } while ((v0 = DelayKt.delay(500L, $continuation)) != var5_5);
                return var5_5;
            }
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }

    @Override
    @Nullable
    public Object onSocketOut(@NotNull Socket socket, @NotNull Continuation<? super Unit> $completion) {
        return SocketListenerImpl.onSocketOut$suspendImpl(this, socket, $completion);
    }

    static /* synthetic */ Object onSocketOut$suspendImpl(SocketListenerImpl $this, Socket socket, Continuation<? super Unit> $completion) {
        LogExtensionsKt.logError("onSocketOut");
        return Unit.INSTANCE;
    }
}
