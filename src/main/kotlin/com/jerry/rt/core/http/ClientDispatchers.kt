package com.jerry.rt.core.http

import com.jerry.rt.core.Context
import com.jerry.rt.extensions.createStandCoroutineScope
import com.jerry.rt.interfaces.RtCoreListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Socket
import java.util.Collections
import java.util.UUID

/**
 * @className: ClientDispatchers
 * @description: socket客户端管理
 * @author: Jerry
 * @date: 2022/12/31:16:41
 **/
internal class ClientDispatchers(private val context: Context,private val rtCoreListener: RtCoreListener) {
    private val scope = createStandCoroutineScope(){
        rtCoreListener.onRtCoreException(it)
    }
    private val clients = Collections.synchronizedList(mutableListOf<Client>())


    private fun generateClientId():String{
        val uuid = UUID.randomUUID().toString()
        return synchronized(ClientDispatchers::class){
            if (clients.find { it.getClientId() == uuid } ==null){
                uuid
            }else{
                generateClientId()
            }
        }
    }

    fun onClientIn(socket:Socket){
        val client = Client(context)
        client.initClient(generateClientId())
        synchronized(ClientDispatchers::class){
            clients.add(client)
        }

        scope.launch(Dispatchers.IO) {
            rtCoreListener.onClientIn(client)
            client.init(socket)
            onClientOut(client)
        }
    }

    private fun onClientOut(client: Client){
        rtCoreListener.onClientOut(client)
        client.close()
        synchronized(ClientDispatchers::class){
            clients.remove(client)
        }
    }

    fun clear(){
        synchronized(ClientDispatchers::class){
            val listIterator = clients.listIterator()
            while (listIterator.hasNext()){
                val next = listIterator.next()
                rtCoreListener.onClientOut(next)
                next.close()
                listIterator.remove()
            }
        }
    }
}