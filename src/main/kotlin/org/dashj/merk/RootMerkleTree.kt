package org.dashj.merk

import io.github.rctcwyvrn.blake3.Blake3
import kotlin.experimental.and

class RootMerkleTree(val elementToProve: ByteArray, val rootTreeProof: ByteArray) {

    companion object {
        fun hash(data: ByteArray): ByteArray {
            val hasher = Blake3.newInstance()
            hasher.update(data)
            return hasher.digest()
        }
        fun double_hash(data: ByteArray): ByteArray {
            return hash(hash(data))
        }
    }

    private var flags: ByteArray
    private val proofHashes = arrayListOf<ByteArray>()

    init {
        var offset = 0
        val leafCount = rootTreeProof.getIntAtLE(offset)
        if (leafCount != 1) {
            throw IllegalArgumentException("Leaf count must be 1, but instead is $leafCount")
        }
        offset += 4

        val (varIntSize, length) = rootTreeProof.getVarInt(offset)
        offset += varIntSize

        for (i in 0 until length) {
            proofHashes.add(rootTreeProof.copyOfRange(offset, offset + 32))
            offset += 32
        }

        // flags
        val (varIntSize2, flagsLength) = rootTreeProof.getVarInt(offset)
        offset += varIntSize2
        flags = rootTreeProof.copyOfRange(offset, (offset + flagsLength).toInt())
    }

    fun merkleRoot(): ByteArray {
        var merkelRoot = elementToProve
        for ((i, leaf) in proofHashes.withIndex()) {
            val proofIsRight = (flags[i / 8] and (1 shl (i % 8)).toByte()).toInt() != 0
            var left: ByteArray
            val right: ByteArray

            if (proofIsRight) {
                right = leaf
                left = merkelRoot
            } else {
                right = merkelRoot
                left = leaf
            }

            val concat = left.reversedArray().plus(right.reversedArray())
            merkelRoot = double_hash(concat).reversedArray()
        }
        return merkelRoot
    }
}
