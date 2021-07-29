package org.dashj.merk

import org.dashj.platform.dpp.toHexString
import org.dashj.platform.dpp.util.HashUtils
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class RootMerkleTreeTest {

    @Test
    fun testMerkleTree() {
        val elements = arrayListOf("a", "b", "c", "d", "e")
        val leaves = arrayListOf<ByteArray>()
        for (element in elements) {
            val elementData = element.toByteArray()
            leaves.add(RootMerkleTree.hash(elementData))
        }
        val firstLeaf = leaves.first()
        val expectedHashes = arrayListOf(
            HashUtils.fromHex("17762fddd969a453925d65717ac3eea21320b66b54342fde15128d6caf21215f"),
            HashUtils.fromHex("10e5cf3d3c8a4f9f3468c8cc58eea84892a22fdadbc1acb22410190044c1d553"),
            HashUtils.fromHex("ea7aa1fc9efdbe106dbb70369a75e9671fa29d52bd55536711bf197477b8f021"),
            HashUtils.fromHex("d5ede538f628f687e5e0422c7755b503653de2dcd7053ca8791afa5d4787d843"),
            HashUtils.fromHex("27bb492e108bf5e9c724176d7ae75d4cedc422fe4065020bd6140c3fcad3a9e7")
        )
        assertListOfByteArraysEquals(expectedHashes, leaves)

        val secondRow = arrayListOf<ByteArray>()
        val secondRowFirstConcatData = leaves[0].reversedArray().plus(leaves[1].reversedArray())
        secondRow.add(RootMerkleTree.double_hash(secondRowFirstConcatData).reversedArray())

        val secondRowSecondConcatData = leaves[2].reversedArray().plus(leaves[3].reversedArray())
        secondRow.add(RootMerkleTree.double_hash(secondRowSecondConcatData).reversedArray())

        val secondRowThirdConcatData = leaves[4].reversedArray().plus(leaves[4].reversedArray())
        secondRow.add(RootMerkleTree.double_hash(secondRowThirdConcatData).reversedArray())

        val expected2ndRowHashes = arrayListOf(
            HashUtils.fromHex("83c76487fb05702e5e955cd9d77d98318a65797ade8e5d22f9abdfe2cf36a8f9"),
            HashUtils.fromHex("e69fe01075cd8c29edb4bd66d4e67d24109a86b9b60e0a568f3ec56638d9bed3"),
            HashUtils.fromHex("f077665d64481f1ea4f3d56dab7924ccd8b4d9e6bef03485558e9215df36f636")
        )

        assertListOfByteArraysEquals(expected2ndRowHashes, secondRow)

        val merkleTreeProofData = HashUtils.fromHex("010000000310e5cf3d3c8a4f9f3468c8cc58eea84892a22fdadbc1acb22410190044c1d553e69fe01075cd8c29edb4bd66d4e67d24109a86b9b60e0a568f3ec56638d9bed3d000dee0c3f75f8ba8755fa7b113f2c5c29afc26519bdef3f6bff249c308dd260107")
        val merkleTree = RootMerkleTree(firstLeaf, merkleTreeProofData)
        val appHash = merkleTree.merkleRoot()
        assertArrayEquals(HashUtils.fromHex("2eb4325d8b759161f1998b400a0ac377d506118eb97f0f855c54e6e6a2bacf19"), appHash)
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
    fun testProofSystem() {
        MerkVerifyProof.init()
        val proofData = HashUtils.fromHex("01e958e15a5e7f012711997ff747c3cba9921044b90c26dfbbf518f7e270fb5a9f02229ae7d97d361daa2e70c3d682f290bbceb5493d88a2a2f9ce560d20e1992fbd100199d48dfa38b7064d9aedbcc74f28a8f1dc3bb103bf7180d1a3d74d804d678fa302e25e4ea30c17a86514960601b92227339ce88d057a12d76561b41d676457061210011a6849de9e9f8c663d06f7dc4c86bf66ccee22c4ad4ff8ea1f4125414956c3ad02a4424847298b8406f057c9375747da7d6e96d61ec3b10d5d5fef1e3fb67907d2100320e3105acf7fe6b61e6a3b9bf8054f59fb9264170945c4b40fb25d58b2ac2a1d47008da56269645820e3105acf7fe6b61e6a3b9bf8054f59fb9264170945c4b40fb25d58b2ac2a1d476762616c616e63651a3b9ac7e6687265766973696f6e006a7075626c69634b65797381a36269640064646174615821032fc3bdf73d86c40bd27fbd62a793356cd625508b2306231167ce4e61af66e55f6474797065006f70726f746f636f6c56657273696f6e0002a5fee690784b06f4765fec55ddbde482b40a7b4bb9ce8ed01440b59aa2f78f881001226345b996d3e565bd98de759767b7daddf2d452e428e542e1872622a1ef2b9511111111")

        val (rootHash, elements) = MerkVerifyProof.extractProofWithHash(proofData)

        val expectedRootHash = HashUtils.fromHex("784f1ad1fc6065d00bf3d4d2e60af5298716a0b973ee9a5e107585e60b921612")
        assertArrayEquals(expectedRootHash, rootHash)

        val rootTreeProof = HashUtils.fromHex("0100000003bfef7d172b666943c33fae47b614259412f52435edd99bbf933144411c3aeab4ffc0c0b0c5053f25cf5be50aead3aabb6b75575e54db7401e2db85094a0cd1ace5bc49ba1d2e2c670b7ab5463de2736125c8582b76c6f3896461fd2ce7049d980103")

        val merkleTree = RootMerkleTree(rootHash, rootTreeProof)

        val appHash = merkleTree.merkleRoot()
        assertArrayEquals(appHash, HashUtils.fromHex("a33ffcc2bdf85f17baf8b8dfa0261b6f61b5b97ac74846fae60b0ec771f44a7c"))
    }
}
