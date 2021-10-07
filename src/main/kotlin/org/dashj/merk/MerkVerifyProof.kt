/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.merk

import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.toBase58
import org.dashj.platform.dpp.toHex

class MerkVerifyProof {
    companion object {
        @JvmStatic
        private external fun verify(bytes: ByteArray, expectedHash: ByteArray, map: Any): Boolean
        @JvmStatic
        private external fun extractProofNative(bytes: ByteArray, map: Any): ByteArray
        @JvmStatic
        private external fun getVersion(): String

        private const val version = "0.21-SNAPSHOT"

        @JvmStatic
        fun init() {
            try {
                System.loadLibrary("dashj_merk")

                val rustVersion = getVersion()
                if (rustVersion != version) {
                    throw RuntimeException("Native rust code version $rustVersion doesn't match Kotlin version $version")
                }
            } catch (e: UnsatisfiedLinkError) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun verifyProof(bytes: ByteArray, expectedHash: ByteArray, map: MutableMap<ByteArrayKey, ByteArray>): Boolean {
            val verifiedMap = HashMap<ByteArray, ByteArray>()
            val verified = verify(bytes, expectedHash, verifiedMap)
            for (entry in verifiedMap) {
                map[ByteArrayKey(entry.key)] = entry.value
            }
            return verified
        }

        @JvmStatic
        fun extractProof(bytes: ByteArray): Map<ByteArrayKey, ByteArray> {
            val map = HashMap<ByteArray, ByteArray>()
            extractProofNative(bytes, map)
            val mapReturn = HashMap<ByteArrayKey, ByteArray>()
            for (entry in map) {
                mapReturn[ByteArrayKey(entry.key)] = entry.value
            }
            return mapReturn
        }

        @JvmStatic
        fun extractProofWithHash(bytes: ByteArray): Pair<ByteArray, Map<ByteArrayKey, ByteArray>> {
            val map = HashMap<ByteArray, ByteArray>()
            val hash = extractProofNative(bytes, map)
            val mapReturn = HashMap<ByteArrayKey, ByteArray>()
            for (entry in map) {
                mapReturn[ByteArrayKey(entry.key)] = entry.value
            }
            return Pair(hash, mapReturn)
        }

        private const val HASH_SIZE = 32

        @kotlin.ExperimentalUnsignedTypes
        fun extractDataDebug(bytes: ByteArray): Map<ByteArrayKey, ByteArray> {
            var i = 0
            val result = hashMapOf<ByteArrayKey, ByteArray>()
            while (i < bytes.size) {
                when (bytes[i].toInt()) {
                    1 -> {
                        i++
                        val hash = bytes.copyOfRange(i, i + HASH_SIZE)
                        i += HASH_SIZE
                        println("Push(Hash(hash)) => 0x01 <32-byte hash>")
                        println("                 =>      ${hash.toHex()}")
                    }
                    2 -> {
                        i++
                        val hash = bytes.copyOfRange(i, i + HASH_SIZE)
                        i += HASH_SIZE
                        println("Push(KVHash(hash)) => 0x02 <32-byte hash>")
                        println("                 =>        ${hash.toHex()}")
                    }
                    3 -> {
                        i++
                        val keySize = bytes[i].toInt()
                        i++
                        val key = bytes.copyOfRange(i, i + keySize)
                        i += keySize
                        val valueSizeLow = bytes[i + 1].toUByte().toInt()
                        val valueSizeHigh = bytes[i].toUByte().toInt()
                        val valueSize = valueSizeHigh * 256 + valueSizeLow
                        i += 2
                        val value = bytes.copyOfRange(i, i + valueSize)
                        i += valueSize

                        println("Push(KV(key, value)) => 0x03 <1-byte key length> <n-byte key> <2-byte value length> <n-byte value>")
                        println("                     =>      $keySize ${key.toHex()} / ${key.toBase58()}")
                        println("                     =>      $valueSize ${value.toHex()}")
                        val map = Factory.decodeProtocolEntity(value, ProtocolVersion.latestVersion)
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

        @kotlin.ExperimentalUnsignedTypes
        @JvmStatic
        fun extractData(bytes: ByteArray): Map<ByteArrayKey, ByteArray> {
            var i = 0
            val result = hashMapOf<ByteArrayKey, ByteArray>()
            while (i < bytes.size) {
                when (bytes[i].toInt()) {
                    1 -> {
                        i++
                        i += HASH_SIZE
                    }
                    2 -> {
                        i++
                        i += HASH_SIZE
                    }
                    3 -> {
                        i++
                        val keySize = bytes[i].toInt()
                        i++
                        val key = bytes.copyOfRange(i, i + keySize)
                        i += keySize
                        val valueSizeLow = bytes[i + 1].toUByte().toInt()
                        val valueSizeHigh = bytes[i].toUByte().toInt()
                        val valueSize = valueSizeHigh * 256 + valueSizeLow
                        i += 2
                        val value = bytes.copyOfRange(i, i + valueSize)
                        i += valueSize

                        result[ByteArrayKey(key)] = value
                    }
                    0x10 -> {
                        i++
                    }
                    0x11 -> {
                        i++
                    }
                    else -> {
                        i++
                    }
                }
            }
            return result
        }
    }
}
