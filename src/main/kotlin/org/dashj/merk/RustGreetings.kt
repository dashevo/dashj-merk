package org.dashj.merk

import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.toBase58
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.Cbor


class RustGreetings {
    fun sayHello(to: String): String {
        return greeting(to)
    }

    companion object {
        @JvmStatic
        private external fun greeting(pattern: String): String
        @JvmStatic
        private external fun verify(bytes: ByteArray, key: ByteArray, expectedHash: ByteArray): ByteArray

        fun decode (bytes: ByteArray): List<ByteArray> {
            var i = 0
            val result = arrayListOf<ByteArray>()
            while (i < bytes.size) {
                when (bytes[i].toInt()) {
                    1 -> {
                        i++;
                        val hash = bytes.copyOfRange(i, i + 20)
                        i += 20
                        println("Push(Hash(hash)) => 0x01 <20-byte hash>")
                        println("                 =>      ${hash.toHexString()}")
                    }
                    2 -> {
                        i++
                        val hash = bytes.copyOfRange(i, i + 20)
                        i += 20
                        println("Push(KVHash(hash)) => 0x02 <20-byte hash>")
                        println("                 =>        ${hash.toHexString()}")
                    }
                    3 -> {
                        i++
                        val keySize = bytes[i].toInt()
                        i++
                        val key = bytes.copyOfRange(i, i + keySize)
                        i += keySize
                        val valueSizeLow = bytes[i].toUByte().toInt()
                        val valueSizeHigh = bytes[i+1].toUByte().toInt()
                        val valueSize = valueSizeHigh * 256 + valueSizeLow;
                        i += 2
                        val value = bytes.copyOfRange(i, i + valueSize)
                        i += valueSize

                        println("Push(KV(key, value)) => 0x03 <1-byte key length> <n-byte key> <2-byte value length> <n-byte value>")
                        println("                     =>      $keySize ${key.toHexString()} / ${key.toBase58()}")
                        println("                     =>      $valueSize ${value.toHexString()}")
                        val map = Cbor.decode(value)
                        println("                     => $map")
                        result.add(value)
                    }
                    0x10 -> {
                        println("Parent => 0x10")
                        i++
                    }
                    0x11 -> {
                        println("Child => 0x11")
                        i++
                    }
                    else -> {
                        println("unknown ${bytes[i]}")
                        i++
                    }
                }
            }
            return result
        }

        fun decodeRootHash (bytes: ByteArray): ByteArray? {
            var i = 0
            while (i < bytes.size) {
                return when (bytes[i].toInt()) {
                    1 -> {
                        i++;
                        val hash = bytes.copyOfRange(i, i + 20)
                        println(hash.toHexString())
                        hash
                    }
                    else -> {
                        println("unknown ${bytes[i]}")
                        null
                    }
                }
            }
            return null
        }

        @JvmStatic
        fun main(args: Array<String>) {
            System.loadLibrary("dashj_merk")
            println(greeting("from Kotlin"))

            val storeTreeProof = byteArrayOf(1, -62, -87, -81, -94, -64, 73, 65, 52, -93, -39, -109, 25, -88, 118, -18, -57, -45, 81, -57, 28, 2, 104, -66, 67, 95, -91, -7, -49, -58, -119, 20, 89, 98, -112, -125, -19, -19, -33, 101, 29, -124, 16, 1, 96, -66, -74, -104, -84, -108, -9, -63, -37, -70, 122, -72, 112, -120, -80, 9, 118, 57, 96, -78, 2, 68, 103, 82, 25, 117, -42, -75, -64, -76, 45, 70, 11, 50, 11, 95, 73, 125, -20, 74, 9, 16, 1, 106, 55, -25, 118, -90, 16, -84, 42, 108, 113, -74, 19, -99, 53, 85, -115, 40, 26, 103, -71, 2, -70, 81, 107, -128, -77, 54, -71, 84, 71, 102, -56, 19, 0, 127, -106, -105, -117, -38, -32, -121, 16, 1, 85, -127, 120, 25, 10, -33, 122, 119, -112, -106, 104, 64, -69, -28, 118, -88, -113, -80, -47, 53, 2, 30, 82, 19, 64, 70, 77, 94, 79, -29, 70, -67, 69, 73, 104, 78, 58, 54, -122, -89, 65, 16, 1, 67, 121, 40, 110, 49, -98, -108, -89, 96, 70, 108, 106, 28, 123, 59, -70, -77, 102, -3, -95, 2, -105, 40, 51, -74, 117, -113, 65, -96, 108, -63, -65, 75, -37, 74, -7, 120, -61, 11, 126, -117, 16, 1, -2, 103, -7, 124, -54, 44, -11, 76, 35, 108, -9, -20, -16, 10, -87, 44, 97, 35, -68, -59, 3, 32, -9, 93, -120, -25, 91, -15, -100, 83, -56, 52, 71, -10, -104, 81, -59, 14, 30, -70, -128, -12, 114, -62, -67, 116, -91, -124, 36, 70, 123, 16, 119, -109, -7, 0, -87, 99, 36, 105, 100, 88, 32, -9, 93, -120, -25, 91, -15, -100, 83, -56, 52, 71, -10, -104, 81, -59, 14, 30, -70, -128, -12, 114, -62, -67, 116, -91, -124, 36, 70, 123, 16, 119, -109, 101, 36, 116, 121, 112, 101, 103, 112, 114, 111, 102, 105, 108, 101, 104, 36, 111, 119, 110, 101, 114, 73, 100, 88, 32, 61, 88, 127, -76, -1, -61, -1, 23, 124, 58, 55, -74, -98, -3, 59, -48, -32, 117, -90, -122, -37, 50, 65, -105, -107, 49, -94, 117, 127, -106, -16, -45, 105, 36, 114, 101, 118, 105, 115, 105, 111, 110, 24, 34, 106, 36, 99, 114, 101, 97, 116, 101, 100, 65, 116, 27, 0, 0, 1, 119, -48, 111, 39, 46, 106, 36, 117, 112, 100, 97, 116, 101, 100, 65, 116, 27, 0, 0, 1, 120, -120, -80, 61, -115, 107, 100, 105, 115, 112, 108, 97, 121, 78, 97, 109, 101, 116, 78, 101, 119, 32, 80, 114, 111, 102, 105, 108, 101, 57, 54, 55, 56, 51, 55, 48, 53, 54, 111, 36, 100, 97, 116, 97, 67, 111, 110, 116, 114, 97, 99, 116, 73, 100, 88, 32, -22, 49, -85, 81, -124, 33, 62, -101, -44, -107, 50, -100, 116, 59, 7, 1, -92, -44, 28, 122, 77, 73, -69, 56, 93, -126, -112, 115, -43, 104, -79, 92, 112, 36, 112, 114, 111, 116, 111, 99, 111, 108, 86, 101, 114, 115, 105, 111, 110, 0, 16, 1, 10, 67, 90, -22, 108, 4, 44, 102, -123, -31, 21, -34, 61, -62, -2, 97, 103, 121, 70, -37, 17, 17, 17, 17, 17, 17)

            val bytes = decodeRootHash(storeTreeProof)!!
            println("Kotlin Parameters that are passed to Rust:")
            println("bytes: ${storeTreeProof.toHexString()}")
            println("key:   ${Identifier.from("HecRbGke5QpiyT3TybLXJLmsRt7JH1FVvUT79qxTwdEa").toBuffer().toHexString()}")
            println("hash:  ${bytes.toHexString()}")
            val result = verify(storeTreeProof, Identifier.from("HecRbGke5QpiyT3TybLXJLmsRt7JH1FVvUT79qxTwdEa").toBuffer(), bytes)
            println(result.toHexString())

            decode(storeTreeProof)
        }
    }
}