package com.theveloper.pixelplay.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

object MulticastSyncManager {
    private var recvJob: Job? = null

    fun startReceiving(scope: CoroutineScope, group: String, port: Int, onCommand: (String) -> Unit) {
        stopReceiving()
        recvJob = scope.launch(Dispatchers.IO) {
            val groupAddr = InetAddress.getByName(group)
            val socket = MulticastSocket(port)
            try {
                socket.joinGroup(groupAddr)
                val buf = ByteArray(1024)
                while (isActive()) {
                    val pkt = DatagramPacket(buf, buf.size)
                    socket.receive(pkt)
                    val s = String(pkt.data, 0, pkt.length)
                    try {
                        onCommand(s)
                    } catch (_: Throwable) {
                    }
                }
            } finally {
                try { socket.leaveGroup(groupAddr) } catch (_: Throwable) {}
                try { socket.close() } catch (_: Throwable) {}
            }
        }
    }

    private fun isActive(): Boolean = recvJob?.isCancelled != true

    fun stopReceiving() {
        recvJob?.cancel()
        recvJob = null
    }

    suspend fun sendNow(group: String, port: Int, message: String) {
        withContext(Dispatchers.IO) {
            DatagramSocket().use { socket ->
                val addr = InetAddress.getByName(group)
                val buf = message.toByteArray()
                val pkt = DatagramPacket(buf, buf.size, addr, port)
                socket.send(pkt)
            }
        }
    }
}
