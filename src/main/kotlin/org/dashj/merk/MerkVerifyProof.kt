package org.dashj.merk

import org.dashevo.dpp.toBase58
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.Cbor


class MerkVerifyProof {
    companion object {
        @JvmStatic
        private external fun verify(bytes: ByteArray, key: ByteArray, expectedHash: ByteArray): ByteArray

        @JvmStatic
        fun init() {
            try {
                System.loadLibrary("dashj_merk")
            } catch (e: Exception) {
                println(e)
            }
        }

        @JvmStatic
        fun verifyProof(bytes: ByteArray, key: ByteArray, expectedHash: ByteArray): ByteArray {
            return verify(bytes, key, expectedHash)
        }

        @kotlin.ExperimentalUnsignedTypes
        fun decode (bytes: ByteArray): Map<ByteArrayKey, ByteArray> {
            var i = 0
            val result = hashMapOf<ByteArrayKey, ByteArray>()
            while (i < bytes.size) {
                when (bytes[i].toInt()) {
                    1 -> {
                        i++
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
                        result[ByteArrayKey(key)] = value
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
    }
}