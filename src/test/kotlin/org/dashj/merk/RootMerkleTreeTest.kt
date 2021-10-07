package org.dashj.merk
/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

import org.dashj.platform.dpp.toHexString
import org.dashj.platform.dpp.util.Converters
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class RootMerkleTreeTest {

    @Test
    fun testMerkleTree() {
        val elements = arrayListOf("a", "b", "c", "d", "e", "f")
        val leaves = arrayListOf<ByteArray>()
        for (element in elements) {
            val elementData = element.toByteArray()
            leaves.add(RootMerkleTree.hash(elementData))
        }
        val expectedHashes = arrayListOf(
            Converters.fromHex("17762fddd969a453925d65717ac3eea21320b66b54342fde15128d6caf21215f"),
            Converters.fromHex("10e5cf3d3c8a4f9f3468c8cc58eea84892a22fdadbc1acb22410190044c1d553"),
            Converters.fromHex("ea7aa1fc9efdbe106dbb70369a75e9671fa29d52bd55536711bf197477b8f021"),
            Converters.fromHex("d5ede538f628f687e5e0422c7755b503653de2dcd7053ca8791afa5d4787d843"),
            Converters.fromHex("27bb492e108bf5e9c724176d7ae75d4cedc422fe4065020bd6140c3fcad3a9e7"),
            Converters.fromHex("9ab388bedc43eaf44150107d17ad090f6b1c34610f5740778ddb95d9f06576ee")
        )
        assertListOfByteArraysEquals(expectedHashes, leaves)

        val secondRow = arrayListOf<ByteArray>()
        val secondRowFirstConcatData = leaves[0].plus(leaves[1])
        secondRow.add(RootMerkleTree.hash(secondRowFirstConcatData))

        val secondRowSecondConcatData = leaves[2].plus(leaves[3])
        secondRow.add(RootMerkleTree.hash(secondRowSecondConcatData))

        val secondRowThirdConcatData = leaves[4].plus(leaves[5])
        secondRow.add(RootMerkleTree.hash(secondRowThirdConcatData))

        val expected2ndRowHashes = arrayListOf(
            Converters.fromHex("8912f1e49d6c94830787bc8765e92f409d6db9041739884a42e59f16388756b1"),
            Converters.fromHex("a77a720d29e9dfa24461260e8ceb053ebf346dca2d81aa2b4182cb491fd43219"),
            Converters.fromHex("d6e299f15660574f2c30adf712fd38c03dbce8447bc79d9bb559e825ffd52a62")
        )

        assertListOfByteArraysEquals(expected2ndRowHashes, secondRow)

        val thirdRow = arrayListOf<ByteArray>()
        val thirdRowFirstConcatData = secondRow[0].plus(secondRow[1])
        thirdRow.add(RootMerkleTree.hash(thirdRowFirstConcatData))
        thirdRow.add(secondRow[2])

        val expected3rdRowHashes = arrayListOf(
            Converters.fromHex("15b05807bd481249f1ad113b96863e0bd70b8ef2d807400d8997c7b8fc0f82b1"),
            Converters.fromHex("d6e299f15660574f2c30adf712fd38c03dbce8447bc79d9bb559e825ffd52a62"),
        )
        assertListOfByteArraysEquals(expected3rdRowHashes, thirdRow)

        val rootRowFirstConcatData = thirdRow[0].plus(thirdRow[1])
        val rootHash = RootMerkleTree.hash(rootRowFirstConcatData)

        assertArrayEquals(Converters.fromHex("f0bba0f0472fad1a198e52266b726fa6eac3da0dd28eb1a2f1bc08d09e7f0c30"), rootHash)

        val elementsToProof = hashMapOf(
            3 to Converters.fromHex("d5ede538f628f687e5e0422c7755b503653de2dcd7053ca8791afa5d4787d843"),
            4 to Converters.fromHex("27bb492e108bf5e9c724176d7ae75d4cedc422fe4065020bd6140c3fcad3a9e7")
        )

        val merkleTreeProofData = Converters.fromHex("ea7aa1fc9efdbe106dbb70369a75e9671fa29d52bd55536711bf197477b8f0219ab388bedc43eaf44150107d17ad090f6b1c34610f5740778ddb95d9f06576ee8912f1e49d6c94830787bc8765e92f409d6db9041739884a42e59f16388756b1")
        val merkleTree = RootMerkleTree(elementsToProof, merkleTreeProofData, 6)
        val appHash = merkleTree.merkelRoot()

        assertArrayEquals(Converters.fromHex("f0bba0f0472fad1a198e52266b726fa6eac3da0dd28eb1a2f1bc08d09e7f0c30"), appHash)
    }

    private fun assertListOfByteArraysEquals(
        expectedHashes: ArrayList<ByteArray>,
        leaves: ArrayList<ByteArray>
    ) {
        for (expectedHash in expectedHashes) {
            var found = false
            for (leaf in leaves) {
                if (leaf.contentEquals(expectedHash)) {
                    found = true
                    break
                }
            }
            if (!found) {
                fail<Nothing>("${expectedHash.toHexString()} was not found")
            }
        }
    }

    @Test
    fun testMerkAVLTreeProof() {
        MerkVerifyProof.init()
        val proofData = Converters.fromHex("014fc2c3d1930a2b5517c4d067ad709750df79a5d4b8999e01c0cc08876f2dbe4a02229ae7d97d361daa2e70c3d682f290bbceb5493d88a2a2f9ce560d20e1992fbd10013167d6ec26570bddc238e48494c404ebe2ae869d17652a9566bf6109e4de55d302e25e4ea30c17a86514960601b92227339ce88d057a12d76561b41d676457061210011a6849de9e9f8c663d06f7dc4c86bf66ccee22c4ad4ff8ea1f4125414956c3ad022cb8354f326e19583b2579ef99a598d63e5a2baa836eb2f2e13055c848208318100320e3105acf7fe6b61e6a3b9bf8054f59fb9264170945c4b40fb25d58b2ac2a1d47008da56269645820e3105acf7fe6b61e6a3b9bf8054f59fb9264170945c4b40fb25d58b2ac2a1d476762616c616e63651a3b9ac7e6687265766973696f6e006a7075626c69634b65797381a36269640064646174615821032fc3bdf73d86c40bd27fbd62a793356cd625508b2306231167ce4e61af66e55f6474797065006f70726f746f636f6c56657273696f6e0002a5fee690784b06f4765fec55ddbde482b40a7b4bb9ce8ed01440b59aa2f78f881001226345b996d3e565bd98de759767b7daddf2d452e428e542e1872622a1ef2b9511111111")

        val (rootHash, elements) = MerkVerifyProof.extractProofWithHash(proofData)

        val expectedRootHash = Converters.fromHex("6ef4c210cb5e919d9dcd894bc841506f93ef3f8638eab452502050b04ee079fb")
        assertArrayEquals(expectedRootHash, rootHash)
    }
}
