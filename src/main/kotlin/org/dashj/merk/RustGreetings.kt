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

            val storeTreeProof = byteArrayOf(1, 122, 90, 84, 98, 38, -48, 10, -55, 98, 55, -64, 13, 127, 58, 19, 37, 81, -4, -63, -62, 2, 63, -17, -71, -108, 3, 83, 91, -36, 7, 24, 103, -32, 21, -75, 27, 111, 68, -40, -125, 33, 16, 1, -81, 44, 107, 97, 106, 44, -13, 111, 68, -92, 48, 44, 125, 62, 70, -100, 25, -68, -125, -36, 2, 61, 39, 19, -93, 68, 93, 67, -8, -118, -97, -42, 51, -15, 126, 23, 41, 12, 41, -4, -2, 16, 1, 125, -102, 75, -107, -86, 32, 113, -38, 14, -2, -91, 82, -71, -103, 33, -35, -1, 61, -39, 37, 3, 32, -113, 74, -94, 91, 110, -40, 35, 109, 49, -122, -92, -71, 12, 30, -116, 5, 7, 7, 43, 16, 42, 87, -106, -84, 86, 120, 117, 90, 5, -1, -12, -106, -5, 0, -87, 99, 36, 105, 100, 88, 32, -113, 74, -94, 91, 110, -40, 35, 109, 49, -122, -92, -71, 12, 30, -116, 5, 7, 7, 43, 16, 42, 87, -106, -84, 86, 120, 117, 90, 5, -1, -12, -106, 101, 36, 116, 121, 112, 101, 103, 112, 114, 111, 102, 105, 108, 101, 104, 36, 111, 119, 110, 101, 114, 73, 100, 88, 32, 79, -65, 122, -103, 62, -8, 55, 103, 47, 66, -14, -103, 56, 121, -69, -127, 68, 68, -36, -40, 57, 8, 52, -39, 64, -108, -43, 7, 45, -55, -65, 125, 105, 36, 114, 101, 118, 105, 115, 105, 111, 110, 8, 106, 36, 99, 114, 101, 97, 116, 101, 100, 65, 116, 27, 0, 0, 1, 121, 71, 119, 87, -55, 106, 36, 117, 112, 100, 97, 116, 101, 100, 65, 116, 27, 0, 0, 1, 121, -112, -51, -14, 110, 107, 100, 105, 115, 112, 108, 97, 121, 78, 97, 109, 101, 119, 88, 97, 110, 100, 101, 114, 32, 72, 97, 114, 114, 105, 115, 45, 51, 54, 52, 48, 48, 49, 54, 55, 54, 111, 36, 100, 97, 116, 97, 67, 111, 110, 116, 114, 97, 99, 116, 73, 100, 88, 32, 88, 75, -84, 106, 27, 106, -99, -62, -78, -49, -62, 74, -117, -12, -78, -32, -70, 47, -89, -92, -106, -47, -23, 72, -118, -85, -89, 94, 72, 53, -88, 68, 112, 36, 112, 114, 111, 116, 111, 99, 111, 108, 86, 101, 114, 115, 105, 111, 110, 0, 16, 1, -60, 19, -116, 99, -109, 110, 119, 4, -53, 99, 29, 21, -126, -37, -87, -51, 99, -42, -1, 85, 17, 2, -3, -75, -110, -22, 61, 90, -122, 38, -72, 111, -99, 111, 90, -40, 59, 99, 115, 105, 114, 0, 16, 1, 66, -72, 61, 126, 58, 39, 5, 106, -38, -19, 125, -117, -30, 99, 55, -15, 19, -58, 51, 3, 17, 17, 2, -81, -121, 113, -101, -43, -49, 127, 7, 2, -3, 17, 106, -31, 65, 39, 97, -69, 65, 38, -25, 16, 1, -50, 68, 115, -34, 79, -112, 123, 55, -101, 54, 118, 89, -123, 127, 55, 56, 71, 3, -27, -105, 17, 2, 59, -85, 117, 116, 66, 84, -78, -9, 26, -125, -118, 82, -19, 100, -59, -54, 95, -62, 109, 35, 16, 1, 95, -14, 45, -91, 4, 87, -23, 79, 18, 3, 5, -70, 125, -91, 46, -123, -81, -83, -35, 98, 17, 17)

            val bytes = decodeRootHash(storeTreeProof)!!
            println("Kotlin Parameters that are passed to Rust:")
            println("bytes: ${storeTreeProof.toHexString()}")
            println("key:   ${Identifier.from("AeMJqwVWRDcvWDu8RUwpxsAe6iAtbGxeY6Xw717Bjj4H").toBuffer().toHexString()}")
            println("hash:  ${bytes.toHexString()}")
            val result = verify(storeTreeProof, Identifier.from("AeMJqwVWRDcvWDu8RUwpxsAe6iAtbGxeY6Xw717Bjj4H").toBuffer(), bytes)
            println(result.toHexString())

            decode(storeTreeProof)
        }
    }
}