/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.merk

import io.github.rctcwyvrn.blake3.Blake3
import java.security.MessageDigest
import kotlin.math.ceil

@Deprecated("use Indices instead in dapi-client")
enum class PlatformDictionary(val value: Int) {
    Contracts(3),
    Documents(4),
    Identities(1),
    PublicKeyHahsesToIdentityIds(2);
}
@Deprecated("use MerkleTree instead in dapi-client")
class RootMerkleTree(
    val elementsToProve: Map<Int, ByteArray>,
    val rootTreeProof: ByteArray,
    val fixedElementCount: Int = 6
) {

    companion object {
        fun hash(data: ByteArray): ByteArray {
            val hasher = Blake3.newInstance()
            hasher.update(data)
            return hasher.digest()
        }
        fun hashTwice(data: ByteArray): ByteArray {
            return hash(hash(data))
        }

        var digest = MessageDigest.getInstance("SHA-256")

        fun sha256(data: ByteArray): ByteArray {
            return digest.digest(data)
        }

        private const val HASH_SIZE = 32
    }

    private val proofHashes = arrayListOf<ByteArray>()

    fun getProofHashes(): List<ByteArray> {
        return proofHashes
    }

    init {
        val length = rootTreeProof.size / HASH_SIZE
        var offset = 0

        for (i in 0 until length) {
            proofHashes.add(rootTreeProof.copyOfRange(offset, offset + HASH_SIZE))
            offset += HASH_SIZE
        }
    }

    fun merkelRoot(): ByteArray {
        var rowElements = elementsToProve
        var rowSize = fixedElementCount

        while (proofHashes.size > 0 || rowElements.size > 1) {
            val nextRowElements = hashMapOf<Int, ByteArray>()
            val positions = rowElements.keys.toMutableList()
            positions.sort()
            var i = 0
            while (i < positions.size) {
                val number = positions[i]
                val storeTreeRootHash = rowElements[number]!!
                var left: ByteArray
                var right: ByteArray
                val pos = number
                if (pos == rowSize - 1) {
                    nextRowElements[pos / 2] = storeTreeRootHash
                    i++
                    continue
                }
                if (number % 2 != 0) {
                    // Right side
                    right = storeTreeRootHash
                    left = proofHashes.first()
                    proofHashes.removeAt(0)
                } else {
                    // Left Side
                    left = storeTreeRootHash
                    if (rowElements[pos + 1] != null) {
                        // Both elements are known, no proof needed
                        right = rowElements[pos + 1]!!
                        i++
                    } else {
                        right = proofHashes.first()
                        proofHashes.removeAt(0)
                    }
                }

                val concat = left.plus(right)
                val merkelRoot = hash(concat)
                nextRowElements[pos / 2] = merkelRoot
                i++
            }
            rowElements = nextRowElements
            rowSize = ceil(rowSize.toDouble() / 2).toInt()
        }
        return rowElements[0]!!
    }
}
